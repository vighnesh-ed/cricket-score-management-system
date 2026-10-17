package com.cricket.cms.controller;

import com.cricket.cms.dto.PlayerResponse;
import com.cricket.cms.dto.PlayerStatsResponse;
import com.cricket.cms.service.PlayerService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/players")
@RequiredArgsConstructor
public class PlayerController {

    private final PlayerService playerService;

    @GetMapping
    public List<PlayerResponse> getPlayers(@RequestParam(required = false) Long teamId) {
        return playerService.getPlayers(teamId);
    }

    @GetMapping("/{id}/stats")
    public PlayerStatsResponse getStats(@PathVariable Long id) {
        return playerService.getStats(id);
    }
}
