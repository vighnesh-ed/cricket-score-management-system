package com.cricket.cms.service;

import com.cricket.cms.model.BallEvent;
import com.cricket.cms.model.Player;

// Batting and bowling figures of one player, always derived from ball events.
class PlayerTally {

    private static final int FOUR = 4;
    private static final int SIX = 6;

    final Player player;
    int runs;
    int ballsFaced;
    int fours;
    int sixes;
    String dismissal;
    int legalBallsBowled;
    int runsConceded;
    int wickets;

    PlayerTally(Player player) {
        this.player = player;
    }

    void addBatting(BallEvent ball) {
        runs += ball.getRuns();
        ballsFaced += ball.countsAsBallFaced() ? 1 : 0;
        fours += ball.getRuns() == FOUR ? 1 : 0;
        sixes += ball.getRuns() == SIX ? 1 : 0;
        if (ball.isWicket()) {
            dismissal = "b " + ball.getBowler().getName();
        }
    }

    void addBowling(BallEvent ball) {
        runsConceded += ball.totalRuns();
        legalBallsBowled += ball.isLegalDelivery() ? 1 : 0;
        wickets += ball.isWicket() ? 1 : 0;
    }

    double strikeRate() {
        return ballsFaced == 0 ? 0.0 : InningsTotals.round2(runs * 100.0 / ballsFaced);
    }

    double economy() {
        return InningsTotals.ratePerOver(runsConceded, legalBallsBowled);
    }

    String overs() {
        return InningsTotals.formatOvers(legalBallsBowled);
    }
}
