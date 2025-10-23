package org.example.oddventure.domain.team.dto;

import org.example.oddventure.domain.team.entity.Team;

public record TeamResponse(Long id, String name) {

    public static TeamResponse of(Team team) {
        return new TeamResponse(team.getId(), team.getName());
    }
}