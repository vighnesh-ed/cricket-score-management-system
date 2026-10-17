package com.cricket.cms.dto;

import java.util.List;

public record InningsScorecard(int inningsNumber, TeamResponse battingTeam, int runs, int wickets, String overs,
        ExtrasResponse extras, List<BattingRow> batting, List<BowlingRow> bowling, List<Integer> runsPerOver) {
}
