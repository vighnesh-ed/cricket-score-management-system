package com.cricket.cms.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.cricket.cms.dto.ScoreResponse;
import com.cricket.cms.exception.BadRequestException;
import com.cricket.cms.exception.InvalidMatchStateException;
import com.cricket.cms.model.BallOutcome;
import com.cricket.cms.model.Match;
import com.cricket.cms.model.MatchStatus;
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

// A 2-over match keeps innings short: 12 legal balls end an innings.
@DataJpaTest(showSql = false)
@ActiveProfiles("test")
@Import({ScoringService.class, ScoreboardService.class})
class ScoringServiceTest {

    private static final int OVERS = 2;

    private final ScoringService scoringService;
    private final ScoreboardService scoreboardService;
    private final TeamRepository teamRepository;
    private final PlayerRepository playerRepository;
    private final MatchRepository matchRepository;

    private Match match;
    private List<Player> india;

    @Autowired
    ScoringServiceTest(ScoringService scoringService, ScoreboardService scoreboardService,
            TeamRepository teamRepository, PlayerRepository playerRepository, MatchRepository matchRepository) {
        this.scoringService = scoringService;
        this.scoreboardService = scoreboardService;
        this.teamRepository = teamRepository;
        this.playerRepository = playerRepository;
        this.matchRepository = matchRepository;
    }

    @BeforeEach
    void setUp() {
        Team indiaTeam = teamRepository.save(new Team("India", "IND"));
        Team australiaTeam = teamRepository.save(new Team("Australia", "AUS"));
        india = createSquad(indiaTeam);
        createSquad(australiaTeam);
        match = matchRepository.save(new Match(indiaTeam, australiaTeam, "Test Ground", OVERS, LocalDateTime.now()));
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

    private void play(BallOutcome outcome, int times) {
        for (int i = 0; i < times; i++) {
            scoringService.recordBall(match.getId(), outcome, null, null);
        }
    }

    private ScoreResponse score() {
        return scoreboardService.getScore(match.getId());
    }

    @Test
    void startMatchMakesMatchLiveWithOpenersAndOpeningBowler() {
        scoringService.startMatch(match.getId());

        ScoreResponse score = score();
        assertEquals(MatchStatus.LIVE, score.status());
        assertEquals(1, score.inningsNumber());
        assertEquals("India", score.battingTeam().name());
        assertEquals("IND 1", score.striker().name());
        assertEquals("IND 2", score.nonStriker().name());
        assertEquals("AUS 11", score.bowler().name());
    }

    @Test
    void startMatchRejectsMatchThatAlreadyStarted() {
        scoringService.startMatch(match.getId());

        assertThrows(InvalidMatchStateException.class, () -> scoringService.startMatch(match.getId()));
    }

    @Test
    void recordBallRejectsMatchThatIsNotLive() {
        assertThrows(InvalidMatchStateException.class,
                () -> scoringService.recordBall(match.getId(), BallOutcome.FOUR, null, null));
    }

    @Test
    void oddRunsSwapStrikeAndEvenRunsDoNot() {
        scoringService.startMatch(match.getId());

        play(BallOutcome.FOUR, 1);
        assertEquals("IND 1", score().striker().name());

        play(BallOutcome.ONE, 1);
        assertEquals("IND 2", score().striker().name());

        play(BallOutcome.THREE, 1);
        ScoreResponse score = score();
        assertEquals("IND 1", score.striker().name());
        assertEquals(5, score.striker().runs());
        assertEquals(2, score.striker().balls());
        assertEquals(3, score.nonStriker().runs());
    }

    @Test
    void endOfOverSwapsStrikeAndChangesBowler() {
        scoringService.startMatch(match.getId());

        play(BallOutcome.DOT, 6);

        ScoreResponse score = score();
        assertEquals("1.0", score.overs());
        assertEquals("IND 2", score.striker().name());
        assertEquals("AUS 10", score.bowler().name());
    }

    @Test
    void singleOffLastBallOfOverKeepsStrikeForNextOver() {
        scoringService.startMatch(match.getId());

        play(BallOutcome.DOT, 5);
        play(BallOutcome.ONE, 1);

        assertEquals("IND 1", score().striker().name());
    }

    @Test
    void wideAndNoBallAddOneRunButAreNotLegalDeliveries() {
        scoringService.startMatch(match.getId());

        play(BallOutcome.WIDE, 1);
        play(BallOutcome.NO_BALL, 1);

        ScoreResponse score = score();
        assertEquals(2, score.runs());
        assertEquals("0.0", score.overs());
        assertEquals("IND 1", score.striker().name());
        assertEquals(1, score.striker().balls());
        assertEquals(List.of("Wd", "Nb"), score.lastSixBalls());

        play(BallOutcome.DOT, 5);
        assertEquals("AUS 11", score().bowler().name());
        play(BallOutcome.DOT, 1);
        assertEquals("AUS 10", score().bowler().name());
    }

    @Test
    void wicketBringsNextBatsmanOnStrike() {
        scoringService.startMatch(match.getId());

        play(BallOutcome.WICKET, 1);

        ScoreResponse score = score();
        assertEquals(1, score.wickets());
        assertEquals("IND 3", score.striker().name());
        assertEquals("IND 2", score.nonStriker().name());
        assertEquals(List.of("W"), score.lastSixBalls());
    }

    @Test
    void inningsEndsWhenOversAreDoneAndChaseStartsWithTarget() {
        scoringService.startMatch(match.getId());

        play(BallOutcome.FOUR, 12);

        ScoreResponse score = score();
        assertEquals(2, score.inningsNumber());
        assertEquals("Australia", score.battingTeam().name());
        assertEquals(49, score.target());
        assertEquals(49, score.runsRequired());
        assertEquals("AUS 1", score.striker().name());
        assertEquals("IND 11", score.bowler().name());
    }

    @Test
    void inningsEndsAtTenWickets() {
        scoringService.startMatch(match.getId());

        play(BallOutcome.WICKET, 10);

        ScoreResponse score = score();
        assertEquals(2, score.inningsNumber());
        assertEquals(1, score.target());
    }

    @Test
    void chaseEndsAsSoonAsTargetIsReached() {
        scoringService.startMatch(match.getId());
        play(BallOutcome.ONE, 12);

        play(BallOutcome.WICKET, 1);
        play(BallOutcome.SIX, 2);
        play(BallOutcome.ONE, 1);

        ScoreResponse score = score();
        assertEquals(MatchStatus.COMPLETED, score.status());
        assertEquals("Australia won by 9 wickets", score.result());
        assertEquals(13, score.runs());
        assertEquals(0, score.runsRequired());
        assertThrows(InvalidMatchStateException.class,
                () -> scoringService.recordBall(match.getId(), BallOutcome.ONE, null, null));
    }

    @Test
    void defendingTeamWinsByRunsWhenChaseRunsOutOfOvers() {
        scoringService.startMatch(match.getId());
        play(BallOutcome.ONE, 12);

        play(BallOutcome.DOT, 12);

        assertEquals("India won by 12 runs", score().result());
    }

    @Test
    void scoresLevelAfterTheChaseIsATie() {
        scoringService.startMatch(match.getId());
        play(BallOutcome.ONE, 12);

        play(BallOutcome.ONE, 12);

        assertEquals("Match tied", score().result());
    }

    @Test
    void allOutChaseHasNoStriker() {
        scoringService.startMatch(match.getId());
        play(BallOutcome.SIX, 12);

        play(BallOutcome.WICKET, 10);

        ScoreResponse score = score();
        assertEquals("India won by 72 runs", score.result());
        assertNull(score.striker());
    }

    @Test
    void manualBatsmanMustBeAtTheCreaseAndNonStrikerTakesStrike() {
        scoringService.startMatch(match.getId());

        scoringService.recordBall(match.getId(), BallOutcome.FOUR, india.get(1).getId(), null);
        ScoreResponse score = score();
        assertEquals("IND 2", score.striker().name());
        assertEquals(4, score.striker().runs());

        assertThrows(BadRequestException.class,
                () -> scoringService.recordBall(match.getId(), BallOutcome.DOT, india.get(5).getId(), null));
    }

    @Test
    void manualBowlerMustBeInTheBowlingTeam() {
        scoringService.startMatch(match.getId());

        assertThrows(BadRequestException.class,
                () -> scoringService.recordBall(match.getId(), BallOutcome.DOT, null, india.get(10).getId()));
    }
}
