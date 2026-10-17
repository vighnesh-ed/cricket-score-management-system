package com.cricket.cms.dto;

import java.util.List;

public record InningsSummary(int inningsNumber, TeamResponse battingTeam, int runs, int wickets, String overs,
        List<Integer> runsPerOver) {
}
