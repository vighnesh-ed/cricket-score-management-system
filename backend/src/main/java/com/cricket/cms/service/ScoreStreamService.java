package com.cricket.cms.service;

import com.cricket.cms.dto.ScoreResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
@RequiredArgsConstructor
public class ScoreStreamService {

    private static final long NO_TIMEOUT = 0L;
    private static final long HEARTBEAT_MS = 15_000;

    private final ScoreboardService scoreboardService;
    private final Map<Long, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(Long matchId) {
        ScoreResponse currentScore = scoreboardService.getScore(matchId);
        SseEmitter emitter = new SseEmitter(NO_TIMEOUT);
        emitter.onCompletion(() -> remove(matchId, emitter));
        emitter.onTimeout(() -> remove(matchId, emitter));
        emitter.onError(error -> remove(matchId, emitter));
        emitters.computeIfAbsent(matchId, id -> new CopyOnWriteArrayList<>()).add(emitter);
        send(matchId, emitter, scoreEvent(currentScore));
        return emitter;
    }

    // Runs only after the scoring transaction has committed, so clients never see a score that was rolled back.
    // That transaction is already finished, so loading the new score needs a transaction of its own.
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public void onScoreChanged(ScoreChangedEvent event) {
        ScoreResponse score = scoreboardService.getScore(event.matchId());
        for (SseEmitter emitter : emitters.getOrDefault(event.matchId(), List.of())) {
            send(event.matchId(), emitter, scoreEvent(score));
        }
    }

    // Proxies and browsers may drop a connection that stays silent for too long.
    @Scheduled(fixedRate = HEARTBEAT_MS, initialDelay = HEARTBEAT_MS)
    public void sendHeartbeat() {
        emitters.forEach((matchId, matchEmitters) -> {
            for (SseEmitter emitter : matchEmitters) {
                send(matchId, emitter, SseEmitter.event().comment("heartbeat"));
            }
        });
    }

    // An event builder can only be sent once, so every client gets a freshly built event.
    private static SseEmitter.SseEventBuilder scoreEvent(ScoreResponse score) {
        return SseEmitter.event().name("score").data(score, MediaType.APPLICATION_JSON);
    }

    // A send fails when the client has gone away. That client is dropped and scoring carries on.
    private void send(Long matchId, SseEmitter emitter, SseEmitter.SseEventBuilder event) {
        try {
            emitter.send(event);
        } catch (IOException | IllegalStateException e) {
            remove(matchId, emitter);
        }
    }

    private void remove(Long matchId, SseEmitter emitter) {
        List<SseEmitter> matchEmitters = emitters.get(matchId);
        if (matchEmitters != null) {
            matchEmitters.remove(emitter);
        }
    }
}
