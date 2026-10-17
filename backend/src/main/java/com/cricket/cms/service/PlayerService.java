package com.cricket.cms.service;

import com.cricket.cms.dto.PlayerResponse;
import com.cricket.cms.dto.PlayerStatsResponse;
import com.cricket.cms.dto.TeamResponse;
import com.cricket.cms.exception.ResourceNotFoundException;
import com.cricket.cms.model.BallEvent;
import com.cricket.cms.model.Player;
import com.cricket.cms.model.Team;
import com.cricket.cms.repository.BallEventRepository;
import com.cricket.cms.repository.PlayerRepository;
import com.cricket.cms.repository.TeamRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlayerService {

    private final PlayerRepository playerRepository;
    private final TeamRepository teamRepository;
    private final BallEventRepository ballEventRepository;

    @Transactional(readOnly = true)
    public List<PlayerResponse> getPlayers(Long teamId) {
        List<Player> players = teamId == null
                ? playerRepository.findAllByOrderByIdAsc()
                : playerRepository.findByTeamOrderByIdAsc(findTeam(teamId));
        return players.stream().map(PlayerResponse::from).toList();
    }

    // Totals over every match the player has batted or bowled in.
    @Transactional(readOnly = true)
    public PlayerStatsResponse getStats(Long playerId) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new ResourceNotFoundException("Player " + playerId + " not found"));
        PlayerTally tally = new PlayerTally(player);
        Set<Long> matchIds = new HashSet<>();
        for (BallEvent ball : ballEventRepository.findInvolving(player)) {
            matchIds.add(ball.getInnings().getMatch().getId());
            if (ball.getBatsman().getId().equals(playerId)) {
                tally.addBatting(ball);
            } else {
                tally.addBowling(ball);
            }
        }
        return new PlayerStatsResponse(player.getId(), player.getName(), TeamResponse.from(player.getTeam()),
                matchIds.size(), tally.runs, tally.ballsFaced, tally.fours, tally.sixes, tally.strikeRate(),
                tally.wickets, tally.overs(), tally.runsConceded, tally.economy());
    }

    private Team findTeam(Long teamId) {
        return teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team " + teamId + " not found"));
    }
}
