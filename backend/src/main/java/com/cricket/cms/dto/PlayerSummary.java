package com.cricket.cms.dto;

import com.cricket.cms.model.Player;

public record PlayerSummary(Long id, String name) {

    public static PlayerSummary from(Player player) {
        return new PlayerSummary(player.getId(), player.getName());
    }
}
