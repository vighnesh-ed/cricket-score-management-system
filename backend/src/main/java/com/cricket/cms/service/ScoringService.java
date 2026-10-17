package com.cricket.cms.service;

import static com.cricket.cms.service.InningsTotals.BALLS_PER_OVER;

import com.cricket.cms.exception.BadRequestException;
import com.cricket.cms.exception.InvalidMatchStateException;
import com.cricket.cms.exception.ResourceNotFoundException;
import com.cricket.cms.model.BallEvent;
import com.cricket.cms.model.BallOutcome;
import com.cricket.cms.model.Innings;
import com.cricket.cms.model.Match;
import com.cricket.cms.model.MatchStatus;
import com.cricket.cms.model.Player;
import com.cricket.cms.model.PlayerRole;
import com.cricket.cms.model.Team;
import com.cricket.cms.repository.BallEventRepository;
import com.cricket.cms.repository.InningsRepository;
import com.cricket.cms.repository.MatchRepository;
import com.cricket.cms.repository.PlayerRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScoringService {

    private static final int MAX_WICKETS = 10;
    private static final List<PlayerRole> BOWLING_ROLES = List.of(PlayerRole.BOWLER, PlayerRole.ALL_ROUNDER);

    private final MatchRepository matchRepository;
    private final InningsRepository inningsRepository;
    private final PlayerRepository playerRepository;
    private final BallEventRepository ballEventRepository;

    @Transactional
    public void startMatch(Long matchId) {
        Match match = findMatch(matchId);
        if (match.getStatus() != MatchStatus.SCHEDULED) {
            throw new InvalidMatchStateException("Match " + matchId + " has already started");
        }
        match.setStatus(MatchStatus.LIVE);
        startInnings(match, 1, match.getTeamA(), match.getTeamB(), null);
    }

    // The only place where a delivery is scored. Used by both the simulator and the manual endpoint.
    @Transactional
    public void recordBall(Long matchId, BallOutcome outcome, Long batsmanId, Long bowlerId) {
        Match match = findMatch(matchId);
        if (match.getStatus() != MatchStatus.LIVE) {
            throw new InvalidMatchStateException("Match " + matchId + " is not live");
        }
        Innings innings = inningsRepository.findFirstByMatchOrderByInningsNumberDesc(match).orElseThrow();
        applyManualSelection(innings, batsmanId, bowlerId);

        InningsTotals before = InningsTotals.from(ballEventRepository.findByInningsOrderByIdAsc(innings));
        BallEvent ball = ballEventRepository.save(newBall(innings, outcome, before.legalBalls()));
        InningsTotals after = before.plus(ball);

        updateBatsmen(innings, ball, after.wickets());
        if (isInningsOver(match, innings, after)) {
            finishInnings(match, innings, after);
        } else if (ball.isLegalDelivery() && after.legalBalls() % BALLS_PER_OVER == 0) {
            endOver(innings, after.legalBalls() / BALLS_PER_OVER);
        }
    }

    private Match findMatch(Long matchId) {
        return matchRepository.findById(matchId)
                .orElseThrow(() -> new ResourceNotFoundException("Match " + matchId + " not found"));
    }

    private void startInnings(Match match, int number, Team battingTeam, Team bowlingTeam, Integer target) {
        List<Player> battingOrder = playerRepository.findByTeamOrderByIdAsc(battingTeam);
        Innings innings = new Innings(match, number, battingTeam, bowlingTeam, target);
        innings.setStriker(battingOrder.get(0));
        innings.setNonStriker(battingOrder.get(1));
        innings.setBowler(bowlerForOver(bowlingTeam, 0));
        inningsRepository.save(innings);
    }

    // Wides and no-balls are re-bowled, so they take the over and ball number of the next legal delivery.
    private BallEvent newBall(Innings innings, BallOutcome outcome, int legalBallsSoFar) {
        int overNumber = legalBallsSoFar / BALLS_PER_OVER;
        int ballNumber = legalBallsSoFar % BALLS_PER_OVER + 1;
        return new BallEvent(innings, overNumber, ballNumber, innings.getStriker(), innings.getBowler(), outcome);
    }

    private void updateBatsmen(Innings innings, BallEvent ball, int wickets) {
        if (ball.isWicket()) {
            innings.setStriker(nextBatsman(innings.getBattingTeam(), wickets));
        } else if (ball.getRuns() % 2 == 1) {
            swapStrike(innings);
        }
    }

    // The openers are batting positions 0 and 1, so after n wickets the next batsman is at position n + 1.
    private Player nextBatsman(Team battingTeam, int wickets) {
        List<Player> battingOrder = playerRepository.findByTeamOrderByIdAsc(battingTeam);
        int next = wickets + 1;
        return next < battingOrder.size() ? battingOrder.get(next) : null;
    }

    private void endOver(Innings innings, int nextOverNumber) {
        swapStrike(innings);
        innings.setBowler(bowlerForOver(innings.getBowlingTeam(), nextOverNumber));
    }

    // Specialist bowlers bat last, so reverse batting order lets them open the bowling.
    // Rotating by over number means no bowler bowls two overs in a row.
    private Player bowlerForOver(Team bowlingTeam, int overNumber) {
        List<Player> bowlers = playerRepository.findByTeamAndRoleInOrderByIdDesc(bowlingTeam, BOWLING_ROLES);
        return bowlers.get(overNumber % bowlers.size());
    }

    private void swapStrike(Innings innings) {
        Player striker = innings.getStriker();
        innings.setStriker(innings.getNonStriker());
        innings.setNonStriker(striker);
    }

    private boolean isInningsOver(Match match, Innings innings, InningsTotals totals) {
        boolean allOut = totals.wickets() == MAX_WICKETS;
        boolean oversDone = totals.legalBalls() == match.getTotalOvers() * BALLS_PER_OVER;
        boolean targetReached = innings.getTarget() != null && totals.runs() >= innings.getTarget();
        return allOut || oversDone || targetReached;
    }

    private void finishInnings(Match match, Innings innings, InningsTotals totals) {
        innings.setCompleted(true);
        log.info("Match {}: innings {} ended at {}/{} in {} overs", match.getId(), innings.getInningsNumber(),
                totals.runs(), totals.wickets(), totals.overs());
        if (innings.getInningsNumber() == 1) {
            startInnings(match, 2, innings.getBowlingTeam(), innings.getBattingTeam(), totals.runs() + 1);
        } else {
            match.setStatus(MatchStatus.COMPLETED);
            match.setResult(resultText(innings, totals));
            log.info("Match {}: {}", match.getId(), match.getResult());
        }
    }

    private String resultText(Innings chase, InningsTotals totals) {
        int target = chase.getTarget();
        if (totals.runs() >= target) {
            return chase.getBattingTeam().getName() + " won by " + count(MAX_WICKETS - totals.wickets(), "wicket");
        }
        if (totals.runs() == target - 1) {
            return "Match tied";
        }
        return chase.getBowlingTeam().getName() + " won by " + count(target - 1 - totals.runs(), "run");
    }

    private static String count(int amount, String word) {
        return amount + " " + word + (amount == 1 ? "" : "s");
    }

    private void applyManualSelection(Innings innings, Long batsmanId, Long bowlerId) {
        if (batsmanId != null) {
            putOnStrike(innings, batsmanId);
        }
        if (bowlerId != null) {
            innings.setBowler(findBowler(innings, bowlerId));
        }
    }

    private void putOnStrike(Innings innings, Long batsmanId) {
        if (innings.getNonStriker().getId().equals(batsmanId)) {
            swapStrike(innings);
        } else if (!innings.getStriker().getId().equals(batsmanId)) {
            throw new BadRequestException("Player " + batsmanId + " is not one of the batsmen at the crease");
        }
    }

    private Player findBowler(Innings innings, Long bowlerId) {
        Player bowler = playerRepository.findById(bowlerId)
                .orElseThrow(() -> new BadRequestException("Player " + bowlerId + " not found"));
        if (!bowler.getTeam().getId().equals(innings.getBowlingTeam().getId())) {
            throw new BadRequestException("Player " + bowlerId + " is not in the bowling team");
        }
        return bowler;
    }
}
