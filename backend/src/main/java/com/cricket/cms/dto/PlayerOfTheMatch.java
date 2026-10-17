package com.cricket.cms.dto;

public record PlayerOfTheMatch(Long playerId, String name, String team, int runs, int wickets, int impact) {
}
