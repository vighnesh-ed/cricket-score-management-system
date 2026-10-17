package com.cricket.cms.service;

import com.cricket.cms.dto.BattingRow;
import com.cricket.cms.dto.BowlingRow;
import com.cricket.cms.dto.InningsScorecard;
import com.cricket.cms.dto.InningsSummary;
import com.cricket.cms.dto.MatchSummaryResponse;
import com.cricket.cms.dto.PlayerOfTheMatch;
import com.cricket.cms.dto.ScorecardResponse;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MatchSummaryService {

    private static final int TOP_COUNT = 3;
    // A wicket is worth about as much as 20 runs when picking the player of the match.
    private static final int RUNS_PER_WICKET = 20;

    private final ScorecardService scorecardService;

    public MatchSummaryResponse getSummary(Long matchId) {
        ScorecardResponse scorecard = scorecardService.getScorecard(matchId);
        List<BattingRow> batters = scorecard.innings().stream()
                .flatMap(innings -> innings.batting().stream())
                .toList();
        List<BowlingRow> bowlers = scorecard.innings().stream()
                .flatMap(innings -> innings.bowling().stream())
                .toList();
        return new MatchSummaryResponse(
                scorecard.matchId(),
                scorecard.status(),
                scorecard.result(),
                scorecard.innings().stream().map(this::inningsSummary).toList(),
                topBatters(batters),
                topBowlers(bowlers),
                playerOfTheMatch(batters, bowlers));
    }

    private InningsSummary inningsSummary(InningsScorecard innings) {
        return new InningsSummary(innings.inningsNumber(), innings.battingTeam(), innings.runs(),
                innings.wickets(), innings.overs(), innings.runsPerOver());
    }

    // Batsmen who never faced a ball are left out, they would tie with real zeros.
    private List<BattingRow> topBatters(List<BattingRow> batters) {
        return batters.stream()
                .filter(row -> row.balls() > 0)
                .sorted(Comparator.comparingInt(BattingRow::runs).reversed().thenComparingInt(BattingRow::balls))
                .limit(TOP_COUNT)
                .toList();
    }

    private List<BowlingRow> topBowlers(List<BowlingRow> bowlers) {
        return bowlers.stream()
                .sorted(Comparator.comparingInt(BowlingRow::wickets).reversed()
                        .thenComparingDouble(BowlingRow::economy))
                .limit(TOP_COUNT)
                .toList();
    }

    // A player bats for one side and bowls for the other, so runs and wickets are added up per player.
    private PlayerOfTheMatch playerOfTheMatch(List<BattingRow> batters, List<BowlingRow> bowlers) {
        Map<Long, PlayerOfTheMatch> candidates = new LinkedHashMap<>();
        for (BattingRow row : batters) {
            candidates.merge(row.playerId(), candidate(row.playerId(), row.name(), row.team(), row.runs(), 0),
                    this::combine);
        }
        for (BowlingRow row : bowlers) {
            candidates.merge(row.playerId(), candidate(row.playerId(), row.name(), row.team(), 0, row.wickets()),
                    this::combine);
        }
        return candidates.values().stream()
                .max(Comparator.comparingInt(PlayerOfTheMatch::impact))
                .orElse(null);
    }

    private PlayerOfTheMatch candidate(Long id, String name, String team, int runs, int wickets) {
        return new PlayerOfTheMatch(id, name, team, runs, wickets, runs + wickets * RUNS_PER_WICKET);
    }

    private PlayerOfTheMatch combine(PlayerOfTheMatch a, PlayerOfTheMatch b) {
        return candidate(a.playerId(), a.name(), a.team(), a.runs() + b.runs(), a.wickets() + b.wickets());
    }
}
