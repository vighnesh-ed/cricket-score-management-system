package com.cricket.cms.dto;

import com.cricket.cms.model.Match;
import com.cricket.cms.model.MatchStatus;
import java.time.LocalDateTime;

public record MatchResponse(
        Long id,
        TeamResponse teamA,
        TeamResponse teamB,
        String venue,
        int totalOvers,
        MatchStatus status,
        LocalDateTime startTime,
        String result) {

    public static MatchResponse from(Match match) {
        return new MatchResponse(
                match.getId(),
                TeamResponse.from(match.getTeamA()),
                TeamResponse.from(match.getTeamB()),
                match.getVenue(),
                match.getTotalOvers(),
                match.getStatus(),
                match.getStartTime(),
                match.getResult());
    }
}
