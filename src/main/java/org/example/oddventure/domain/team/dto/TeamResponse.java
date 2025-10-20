package org.example.oddventure.domain.team.dto;

import lombok.Builder;
import lombok.Getter;
import org.example.oddventure.domain.team.entity.Team;

@Getter
public class TeamResponse {
    private Long id;
    private String name;

    @Builder
    public TeamResponse(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public static TeamResponse of(Team team) {
        return TeamResponse.builder()
                .id(team.getId())
                .name(team.getName())
                .build();
    }
}
