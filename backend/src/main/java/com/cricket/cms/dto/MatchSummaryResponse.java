package com.cricket.cms.dto;

import com.cricket.cms.model.MatchStatus;
import java.util.List;

public record MatchSummaryResponse(Long matchId, MatchStatus status, String result, List<InningsSummary> innings,
        List<BattingRow> topBatters, List<BowlingRow> topBowlers, PlayerOfTheMatch playerOfTheMatch) {
}
