package org.example.oddventure.domain.grid.dto.response;

import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record MatchFetchResponse(
        Long fetchId,
        String matchName,
        String teamA,
        String teamB,
        LocalDateTime startTime
) {
}
