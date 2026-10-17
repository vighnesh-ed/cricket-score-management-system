package com.cricket.cms.controller;

import com.cricket.cms.dto.BallRequest;
import com.cricket.cms.dto.MatchResponse;
import com.cricket.cms.dto.ScoreResponse;
import com.cricket.cms.service.MatchService;
import com.cricket.cms.service.ScoreboardService;
import com.cricket.cms.service.ScoringService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/matches")
@RequiredArgsConstructor
public class MatchController {

    private final MatchService matchService;
    private final ScoringService scoringService;
    private final ScoreboardService scoreboardService;

    @GetMapping
    public List<MatchResponse> getAllMatches() {
        return matchService.getAllMatches();
    }

    @GetMapping("/{id}/score")
    public ScoreResponse getScore(@PathVariable Long id) {
        return scoreboardService.getScore(id);
    }

    @PostMapping("/{id}/start")
    public ScoreResponse startMatch(@PathVariable Long id) {
        scoringService.startMatch(id);
        return scoreboardService.getScore(id);
    }

    @PostMapping("/{id}/ball")
    public ScoreResponse recordBall(@PathVariable Long id, @Valid @RequestBody BallRequest request) {
        scoringService.recordBall(id, request.outcome(), request.batsmanId(), request.bowlerId());
        return scoreboardService.getScore(id);
    }
}
