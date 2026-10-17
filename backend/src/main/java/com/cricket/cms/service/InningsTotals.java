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
        return formatOvers(legalBalls);
    }

    public double runRate() {
        return ratePerOver(runs, legalBalls);
    }

    // Cricket notation: 14 legal balls is "2.2" overs, not 2.33.
    public static String formatOvers(int legalBalls) {
        return legalBalls / BALLS_PER_OVER + "." + legalBalls % BALLS_PER_OVER;
    }

    // Runs per over, rounded to 2 decimals. Used for run rate and bowling economy.
    public static double ratePerOver(int runs, int legalBalls) {
        if (legalBalls == 0) {
            return 0.0;
        }
        return round2(runs * (double) BALLS_PER_OVER / legalBalls);
    }

    public static double round2(double value) {
        return Math.round(value * 100) / 100.0;
    }
}
