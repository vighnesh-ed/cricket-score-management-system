package com.cricket.cms.dto;

public record BattingRow(Long playerId, String name, String team, int runs, int balls, int fours, int sixes,
        double strikeRate, String dismissal) {
}
