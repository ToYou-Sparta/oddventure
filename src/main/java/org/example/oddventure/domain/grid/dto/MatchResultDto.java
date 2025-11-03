package org.example.oddventure.domain.grid.dto;

import lombok.Builder;

@Builder
public record MatchResultDto(
        Long fetchId,
        boolean finished,
        String winner,
        String loser
) {
}
