package org.example.oddventure.domain.team.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.oddventure.domain.match.entity.Match;

@Entity
@Getter
@NoArgsConstructor()
public class Team {
    @Id
    @GeneratedValue
    private Long id;

    private String name;

    @Builder
    public Team(String name) {
        this.name = name;
    }

    public static Team of(String name) {
        return Team.builder().name(name).build();
    }
}
