package com.cricket.cms.service;

import com.cricket.cms.model.BallOutcome;
import com.cricket.cms.model.Match;
import com.cricket.cms.model.MatchStatus;
import com.cricket.cms.repository.MatchRepository;
import java.util.EnumMap;
import java.util.Map;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "cricket.simulator.enabled", havingValue = "true", matchIfMissing = true)
public class MatchSimulator {

    // Chances out of 100 deliveries. They average about 165 runs and 5 wickets per 20-over innings.
    private static final Map<BallOutcome, Integer> WEIGHTS = new EnumMap<>(Map.of(
            BallOutcome.DOT, 32,
            BallOutcome.ONE, 35,
            BallOutcome.TWO, 7,
            BallOutcome.THREE, 1,
            BallOutcome.FOUR, 12,
            BallOutcome.SIX, 5,
            BallOutcome.WICKET, 4,
            BallOutcome.WIDE, 3,
            BallOutcome.NO_BALL, 1));
    private static final int TOTAL_WEIGHT = WEIGHTS.values().stream().mapToInt(Integer::intValue).sum();

    private final MatchRepository matchRepository;
    private final ScoringService scoringService;
    private final Random random = new Random();

    @Scheduled(fixedRateString = "${cricket.simulator.rate-ms}")
    public void playNextBall() {
        for (Match match : matchRepository.findByStatus(MatchStatus.LIVE)) {
            try {
                scoringService.recordBall(match.getId(), randomOutcome(), null, null);
            } catch (RuntimeException e) {
                log.warn("Skipped simulated ball for match {}: {}", match.getId(), e.getMessage());
            }
        }
    }

    private BallOutcome randomOutcome() {
        int roll = random.nextInt(TOTAL_WEIGHT);
        for (Map.Entry<BallOutcome, Integer> entry : WEIGHTS.entrySet()) {
            roll -= entry.getValue();
            if (roll < 0) {
                return entry.getKey();
            }
        }
        throw new IllegalStateException("Outcome weights are inconsistent");
    }
}
