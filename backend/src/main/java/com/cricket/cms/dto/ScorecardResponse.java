package com.cricket.cms.dto;

import com.cricket.cms.model.MatchStatus;
import java.util.List;

public record ScorecardResponse(Long matchId, MatchStatus status, String result, List<InningsScorecard> innings) {
}
