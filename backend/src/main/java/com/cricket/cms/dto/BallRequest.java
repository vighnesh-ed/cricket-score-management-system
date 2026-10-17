package com.cricket.cms.dto;

import com.cricket.cms.model.BallOutcome;
import jakarta.validation.constraints.NotNull;

// batsmanId and bowlerId are optional; without them the current striker and bowler are used.
public record BallRequest(@NotNull BallOutcome outcome, Long batsmanId, Long bowlerId) {
}
