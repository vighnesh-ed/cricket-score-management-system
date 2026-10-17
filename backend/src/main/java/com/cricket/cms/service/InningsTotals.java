package com.cricket.cms.service;

import com.cricket.cms.model.BallEvent;
import java.util.List;

// Runs, wickets and legal balls of an innings, always derived from its ball events.
public record InningsTotals(int runs, int wickets, int legalBalls) {

    public static final int BALLS_PER_OVER = 6;

    public static InningsTotals from(List<BallEvent> balls) {
        InningsTotals totals = new InningsTotals(0, 0, 0);
        for (BallEvent ball : balls) {
            totals = totals.plus(ball);
        }
        return totals;
    }

    public InningsTotals plus(BallEvent ball) {
        return new InningsTotals(
                runs + ball.totalRuns(),
                wickets + (ball.isWicket() ? 1 : 0),
                legalBalls + (ball.isLegalDelivery() ? 1 : 0));
    }

    public String overs() {
        return legalBalls / BALLS_PER_OVER + "." + legalBalls % BALLS_PER_OVER;
    }

    public double runRate() {
        if (legalBalls == 0) {
            return 0.0;
        }
        double rate = runs * (double) BALLS_PER_OVER / legalBalls;
        return Math.round(rate * 100) / 100.0;
    }
}
