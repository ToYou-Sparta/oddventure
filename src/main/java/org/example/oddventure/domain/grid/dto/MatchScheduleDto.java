package org.example.oddventure.domain.grid.dto;

import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record MatchScheduleDto(
        Long fetchId,
        String matchName,
        String teamA,
        String teamB,
        LocalDateTime startTime
) {
}
