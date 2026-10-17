package com.cricket.cms.service;

import com.cricket.cms.dto.BattingRow;
import com.cricket.cms.dto.BowlingRow;
import com.cricket.cms.dto.ExtrasResponse;
import com.cricket.cms.dto.InningsScorecard;
import com.cricket.cms.dto.ScorecardResponse;
import com.cricket.cms.dto.TeamResponse;
import com.cricket.cms.exception.ResourceNotFoundException;
import com.cricket.cms.model.BallEvent;
import com.cricket.cms.model.ExtraType;
import com.cricket.cms.model.Innings;
import com.cricket.cms.model.Match;
import com.cricket.cms.model.Player;
import com.cricket.cms.repository.BallEventRepository;
import com.cricket.cms.repository.InningsRepository;
import com.cricket.cms.repository.MatchRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ScorecardService {

    private static final String NOT_OUT = "not out";

    private final MatchRepository matchRepository;
    private final InningsRepository inningsRepository;
    private final BallEventRepository ballEventRepository;

    @Transactional(readOnly = true)
    public ScorecardResponse getScorecard(Long matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ResourceNotFoundException("Match " + matchId + " not found"));
        List<InningsScorecard> innings = inningsRepository.findByMatchOrderByInningsNumberAsc(match).stream()
                .map(this::inningsScorecard)
                .toList();
        return new ScorecardResponse(match.getId(), match.getStatus(), match.getResult(), innings);
    }

    private InningsScorecard inningsScorecard(Innings innings) {
        List<BallEvent> balls = ballEventRepository.findByInningsOrderByIdAsc(innings);
        InningsTotals totals = InningsTotals.from(balls);
        return new InningsScorecard(
                innings.getInningsNumber(),
                TeamResponse.from(innings.getBattingTeam()),
                totals.runs(),
                totals.wickets(),
                totals.overs(),
                extras(balls),
                battingRows(innings, balls),
                bowlingRows(balls),
                runsPerOver(balls));
    }

    // Batters appear in batting order (player ids), including not out batsmen who have not faced a ball.
    private List<BattingRow> battingRows(Innings innings, List<BallEvent> balls) {
        Map<Long, PlayerTally> tallies = new LinkedHashMap<>();
        for (BallEvent ball : balls) {
            tallyFor(tallies, ball.getBatsman()).addBatting(ball);
        }
        tallyFor(tallies, innings.getStriker());
        tallyFor(tallies, innings.getNonStriker());
        return tallies.values().stream()
                .sorted(Comparator.comparing(tally -> tally.player.getId()))
                .map(this::battingRow)
                .toList();
    }

    private List<BowlingRow> bowlingRows(List<BallEvent> balls) {
        Map<Long, PlayerTally> tallies = new LinkedHashMap<>();
        for (BallEvent ball : balls) {
            tallyFor(tallies, ball.getBowler()).addBowling(ball);
        }
        return tallies.values().stream().map(this::bowlingRow).toList();
    }

    // The striker or non-striker is null once the batting side is all out.
    private PlayerTally tallyFor(Map<Long, PlayerTally> tallies, Player player) {
        if (player == null) {
            return null;
        }
        return tallies.computeIfAbsent(player.getId(), id -> new PlayerTally(player));
    }

    private BattingRow battingRow(PlayerTally tally) {
        return new BattingRow(tally.player.getId(), tally.player.getName(), tally.player.getTeam().getShortName(),
                tally.runs, tally.ballsFaced, tally.fours, tally.sixes, tally.strikeRate(),
                tally.dismissal == null ? NOT_OUT : tally.dismissal);
    }

    private BowlingRow bowlingRow(PlayerTally tally) {
        return new BowlingRow(tally.player.getId(), tally.player.getName(), tally.player.getTeam().getShortName(),
                tally.overs(), tally.runsConceded, tally.wickets, tally.economy());
    }

    private ExtrasResponse extras(List<BallEvent> balls) {
        int wides = extraRuns(balls, ExtraType.WIDE);
        int noBalls = extraRuns(balls, ExtraType.NO_BALL);
        return new ExtrasResponse(wides, noBalls, wides + noBalls);
    }

    private int extraRuns(List<BallEvent> balls, ExtraType type) {
        return balls.stream().filter(ball -> ball.getExtraType() == type).mapToInt(BallEvent::getExtraRuns).sum();
    }

    // Wides and no-balls carry the over number of the next legal delivery, so every ball falls in the right over.
    private List<Integer> runsPerOver(List<BallEvent> balls) {
        List<Integer> runs = new ArrayList<>();
        for (BallEvent ball : balls) {
            while (runs.size() <= ball.getOverNumber()) {
                runs.add(0);
            }
            runs.set(ball.getOverNumber(), runs.get(ball.getOverNumber()) + ball.totalRuns());
        }
        return runs;
    }
}
