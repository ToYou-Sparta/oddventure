package org.example.oddventure.domain.match.dto.event;

import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record MatchStartEventDto(
        Long matchId,
        LocalDateTime startTime
) {
    public static MatchStartEventDto from(Long matchId, LocalDateTime startTime) {
        return MatchStartEventDto.builder()
                .matchId(matchId)
                .startTime(startTime)
                .build();
    }
}
