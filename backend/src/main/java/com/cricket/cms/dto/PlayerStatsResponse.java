package com.cricket.cms.dto;

public record PlayerStatsResponse(Long playerId, String name, TeamResponse team, int matches, int runs, int balls,
        int fours, int sixes, double strikeRate, int wickets, String oversBowled, int runsConceded,
        double economy) {
}
