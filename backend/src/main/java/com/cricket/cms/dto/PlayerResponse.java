package com.cricket.cms.dto;

import com.cricket.cms.model.Player;
import com.cricket.cms.model.PlayerRole;

public record PlayerResponse(Long id, String name, PlayerRole role, TeamResponse team) {

    public static PlayerResponse from(Player player) {
        return new PlayerResponse(player.getId(), player.getName(), player.getRole(),
                TeamResponse.from(player.getTeam()));
    }
}
