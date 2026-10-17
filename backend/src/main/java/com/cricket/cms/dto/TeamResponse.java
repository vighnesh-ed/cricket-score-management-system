package com.cricket.cms.dto;

import com.cricket.cms.model.Team;

public record TeamResponse(Long id, String name, String shortName) {

    public static TeamResponse from(Team team) {
        return new TeamResponse(team.getId(), team.getName(), team.getShortName());
    }
}
