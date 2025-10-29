package org.example.oddventure.domain.grid.dto.response.field;

import java.util.List;

public record SeriesState(
        List<Team> teams
) {
    public record Team(
            String name,
            boolean won
    ) {
    }
}
