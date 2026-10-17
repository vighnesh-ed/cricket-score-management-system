package com.cricket.cms.dto;

public record BowlingRow(Long playerId, String name, String team, String overs, int runs, int wickets,
        double economy) {
}
