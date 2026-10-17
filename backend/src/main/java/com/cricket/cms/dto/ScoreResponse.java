package com.cricket.cms.dto;

import com.cricket.cms.model.MatchStatus;
import java.util.List;
import lombok.Builder;

// target and runsRequired are null in the first innings.
@Builder
public record ScoreResponse(
        Long matchId,
        MatchStatus status,
        String result,
        int inningsNumber,
        TeamResponse battingTeam,
        int runs,
        int wickets,
        String overs,
        double runRate,
        BatsmanScore striker,
        BatsmanScore nonStriker,
        PlayerSummary bowler,
        List<String> lastSixBalls,
        Integer target,
        Integer runsRequired) {
}
