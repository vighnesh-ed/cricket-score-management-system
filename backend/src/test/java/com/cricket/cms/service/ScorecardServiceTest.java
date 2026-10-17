package com.cricket.cms.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.cricket.cms.dto.BattingRow;
import com.cricket.cms.dto.BowlingRow;
import com.cricket.cms.dto.InningsScorecard;
import com.cricket.cms.dto.MatchSummaryResponse;
import com.cricket.cms.dto.PlayerStatsResponse;
import com.cricket.cms.dto.ScorecardResponse;
import com.cricket.cms.exception.ResourceNotFoundException;
import com.cricket.cms.model.BallOutcome;
import com.cricket.cms.model.Match;
import com.cricket.cms.model.Player;
import com.cricket.cms.model.PlayerRole;
import com.cricket.cms.model.Team;
import com.cricket.cms.repository.MatchRepository;
import com.cricket.cms.repository.PlayerRepository;
import com.cricket.cms.repository.TeamRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

// A 2-over match keeps innings short. The played match is the same in every test:
//   India 17/1 (2.0): IND 1 makes 15 off 10, IND 2 is bowled first, IND 3 is not out on 0 off 2.
//   AUS 11 bowls over 1 (12 runs, 1 wicket, 1 wide), AUS 10 bowls over 2 (5 runs, 1 no-ball).
//   Australia chase 18 with three sixes from AUS 1 and win by 10 wickets.
@DataJpaTest(showSql = false)
@ActiveProfiles("test")
@Import({ScoringService.class, ScorecardService.class, MatchSummaryService.class, PlayerService.class})
class ScorecardServiceTest {

    private final ScoringService scoringService;
    private final ScorecardService scorecardService;
    private final MatchSummaryService matchSummaryService;
    private final PlayerService playerService;
    private final TeamRepository teamRepository;
    private final PlayerRepository playerRepository;
    private final MatchRepository matchRepository;

    private Match match;
    private List<Player> india;
    private List<Player> australia;

    @Autowired
    ScorecardServiceTest(ScoringService scoringService, ScorecardService scorecardService,
            MatchSummaryService matchSummaryService, PlayerService playerService, TeamRepository teamRepository,
            PlayerRepository playerRepository, MatchRepository matchRepository) {
        this.scoringService = scoringService;
        this.scorecardService = scorecardService;
        this.matchSummaryService = matchSummaryService;
        this.playerService = playerService;
        this.teamRepository = teamRepository;
        this.playerRepository = playerRepository;
        this.matchRepository = matchRepository;
    }

    @BeforeEach
    void playMatch() {
        Team indiaTeam = teamRepository.save(new Team("India", "IND"));
        Team australiaTeam = teamRepository.save(new Team("Australia", "AUS"));
        india = createSquad(indiaTeam);
        australia = createSquad(australiaTeam);
        match = matchRepository.save(new Match(indiaTeam, australiaTeam, "Test Ground", 2, LocalDateTime.now()));

        scoringService.startMatch(match.getId());
        play(BallOutcome.FOUR, BallOutcome.WIDE, BallOutcome.SIX, BallOutcome.ONE, BallOutcome.WICKET,
                BallOutcome.DOT, BallOutcome.DOT);
        play(BallOutcome.NO_BALL, BallOutcome.FOUR, BallOutcome.DOT, BallOutcome.DOT, BallOutcome.DOT,
                BallOutcome.DOT, BallOutcome.DOT);
        play(BallOutcome.SIX, BallOutcome.SIX, BallOutcome.SIX);
    }

    // Players 1-6 bat, 7-11 bowl. Names are like "IND 1", in batting order.
    private List<Player> createSquad(Team team) {
        List<Player> squad = new ArrayList<>();
        for (int position = 1; position <= 11; position++) {
            PlayerRole role = position <= 6 ? PlayerRole.BATSMAN : PlayerRole.BOWLER;
            squad.add(new Player(team.getShortName() + " " + position, role, team));
        }
        return playerRepository.saveAll(squad);
    }

    private void play(BallOutcome... outcomes) {
        for (BallOutcome outcome : outcomes) {
            scoringService.recordBall(match.getId(), outcome, null, null);
        }
    }

    @Test
    void scorecardHasBattingRowsInBattingOrderWithHowOut() {
        InningsScorecard first = scorecardService.getScorecard(match.getId()).innings().get(0);

        assertEquals(List.of("IND 1", "IND 2", "IND 3"), first.batting().stream().map(BattingRow::name).toList());
        BattingRow openers = first.batting().get(0);
        assertEquals(15, openers.runs());
        assertEquals(10, openers.balls());
        assertEquals(2, openers.fours());
        assertEquals(1, openers.sixes());
        assertEquals(150.0, openers.strikeRate());
        assertEquals("not out", openers.dismissal());

        BattingRow dismissed = first.batting().get(1);
        assertEquals("b AUS 11", dismissed.dismissal());
        assertEquals(0, dismissed.runs());
        assertEquals(1, dismissed.balls());

        BattingRow waiting = first.batting().get(2);
        assertEquals(2, waiting.balls());
        assertEquals("not out", waiting.dismissal());
    }

    @Test
    void scorecardHasBowlingRowsExtrasAndTotal() {
        InningsScorecard first = scorecardService.getScorecard(match.getId()).innings().get(0);

        assertEquals(17, first.runs());
        assertEquals(1, first.wickets());
        assertEquals("2.0", first.overs());
        assertEquals(1, first.extras().wides());
        assertEquals(1, first.extras().noBalls());
        assertEquals(2, first.extras().total());

        BowlingRow firstBowler = first.bowling().get(0);
        assertEquals("AUS 11", firstBowler.name());
        assertEquals("1.0", firstBowler.overs());
        assertEquals(12, firstBowler.runs());
        assertEquals(1, firstBowler.wickets());
        assertEquals(12.0, firstBowler.economy());

        BowlingRow secondBowler = first.bowling().get(1);
        assertEquals("AUS 10", secondBowler.name());
        assertEquals(5, secondBowler.runs());
        assertEquals(5.0, secondBowler.economy());
    }

    @Test
    void secondInningsOversAreShownInCricketNotation() {
        InningsScorecard chase = scorecardService.getScorecard(match.getId()).innings().get(1);

        assertEquals("0.3", chase.overs());
        assertEquals(18, chase.runs());
        BowlingRow bowler = chase.bowling().get(0);
        assertEquals("0.3", bowler.overs());
        assertEquals(36.0, bowler.economy());
    }

    @Test
    void notOutNonStrikerWhoNeverFacedIsListedWithZeros() {
        InningsScorecard chase = scorecardService.getScorecard(match.getId()).innings().get(1);

        BattingRow waiting = chase.batting().get(1);
        assertEquals("AUS 2", waiting.name());
        assertEquals(0, waiting.balls());
        assertEquals(0.0, waiting.strikeRate());
        assertEquals("not out", waiting.dismissal());
    }

    @Test
    void scorecardOfMatchThatHasNotStartedHasNoInnings() {
        Match fresh = matchRepository.save(new Match(match.getTeamA(), match.getTeamB(), "Other", 2,
                LocalDateTime.now()));

        ScorecardResponse scorecard = scorecardService.getScorecard(fresh.getId());

        assertEquals(List.of(), scorecard.innings());
    }

    @Test
    void scorecardRejectsUnknownMatch() {
        assertThrows(ResourceNotFoundException.class, () -> scorecardService.getScorecard(999L));
    }

    @Test
    void summaryHasResultInningsTotalsAndRunsPerOver() {
        MatchSummaryResponse summary = matchSummaryService.getSummary(match.getId());

        assertEquals("Australia won by 10 wickets", summary.result());
        assertEquals(2, summary.innings().size());
        assertEquals(17, summary.innings().get(0).runs());
        assertEquals(List.of(12, 5), summary.innings().get(0).runsPerOver());
        assertEquals(18, summary.innings().get(1).runs());
        assertEquals(List.of(18), summary.innings().get(1).runsPerOver());
    }

    @Test
    void summaryListsTopThreeBattersAndBowlers() {
        MatchSummaryResponse summary = matchSummaryService.getSummary(match.getId());

        assertEquals(List.of("AUS 1", "IND 1", "IND 2"), summary.topBatters().stream().map(BattingRow::name).toList());
        assertEquals(List.of("AUS 11", "AUS 10", "IND 11"),
                summary.topBowlers().stream().map(BowlingRow::name).toList());
    }

    @Test
    void playerOfTheMatchIsTheHighestImpactPlayer() {
        // AUS 11's wicket (20) beats AUS 1's 18 runs.
        var best = matchSummaryService.getSummary(match.getId()).playerOfTheMatch();

        assertEquals("AUS 11", best.name());
        assertEquals(1, best.wickets());
        assertEquals(20, best.impact());
    }

    @Test
    void summaryOfMatchThatHasNotStartedHasNoPlayerOfTheMatch() {
        Match fresh = matchRepository.save(new Match(match.getTeamA(), match.getTeamB(), "Other", 2,
                LocalDateTime.now()));

        MatchSummaryResponse summary = matchSummaryService.getSummary(fresh.getId());

        assertNull(summary.playerOfTheMatch());
        assertEquals(List.of(), summary.topBatters());
    }

    @Test
    void summaryRejectsUnknownMatch() {
        assertThrows(ResourceNotFoundException.class, () -> matchSummaryService.getSummary(999L));
    }

    @Test
    void batterStatsAreDerivedFromBallEvents() {
        PlayerStatsResponse stats = playerService.getStats(india.get(0).getId());

        assertEquals(1, stats.matches());
        assertEquals(15, stats.runs());
        assertEquals(10, stats.balls());
        assertEquals(2, stats.fours());
        assertEquals(1, stats.sixes());
        assertEquals(150.0, stats.strikeRate());
        assertEquals(0, stats.wickets());
        assertEquals("0.0", stats.oversBowled());
        assertEquals(0.0, stats.economy());
    }

    @Test
    void bowlerStatsAreDerivedFromBallEvents() {
        PlayerStatsResponse stats = playerService.getStats(australia.get(10).getId());

        assertEquals(1, stats.matches());
        assertEquals(1, stats.wickets());
        assertEquals("1.0", stats.oversBowled());
        assertEquals(12, stats.runsConceded());
        assertEquals(12.0, stats.economy());
        assertEquals(0, stats.runs());
    }

    @Test
    void playerWhoNeverPlayedHasZeroStats() {
        PlayerStatsResponse stats = playerService.getStats(india.get(7).getId());

        assertEquals(0, stats.matches());
        assertEquals(0, stats.runs());
        assertEquals(0.0, stats.strikeRate());
        assertEquals("0.0", stats.oversBowled());
    }

    @Test
    void statsRejectUnknownPlayer() {
        assertThrows(ResourceNotFoundException.class, () -> playerService.getStats(999L));
    }

    @Test
    void playersCanBeFilteredByTeam() {
        assertEquals(22, playerService.getPlayers(null).size());
        assertEquals(11, playerService.getPlayers(match.getTeamA().getId()).size());
        assertThrows(ResourceNotFoundException.class, () -> playerService.getPlayers(999L));
    }
}
