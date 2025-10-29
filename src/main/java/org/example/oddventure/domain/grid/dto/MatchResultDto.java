package org.example.oddventure.domain.grid.dto;

import lombok.Builder;

@Builder
public record MatchResultDto(
        Long fetchId,
        String winner,
        String loser
) {
}
