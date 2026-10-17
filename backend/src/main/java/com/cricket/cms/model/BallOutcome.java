package com.cricket.cms.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BallOutcome {
    DOT(0, ExtraType.NONE, false),
    ONE(1, ExtraType.NONE, false),
    TWO(2, ExtraType.NONE, false),
    THREE(3, ExtraType.NONE, false),
    FOUR(4, ExtraType.NONE, false),
    SIX(6, ExtraType.NONE, false),
    WICKET(0, ExtraType.NONE, true),
    WIDE(0, ExtraType.WIDE, false),
    NO_BALL(0, ExtraType.NO_BALL, false);

    private final int runs;
    private final ExtraType extraType;
    private final boolean wicket;

    // A wide or no-ball gives the batting side one penalty run.
    public int getExtraRuns() {
        return extraType == ExtraType.NONE ? 0 : 1;
    }
}
