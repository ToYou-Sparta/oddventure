package org.example.oddventure.domain.match.event.dto;

import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record MatchStartEventDto(
        Long fetchId,
        LocalDateTime startTime
) {
    public static MatchStartEventDto from(Long fetchId, LocalDateTime startTime) {
        return MatchStartEventDto.builder()
                .fetchId(fetchId)
                .startTime(startTime)
                .build();
    }
}
