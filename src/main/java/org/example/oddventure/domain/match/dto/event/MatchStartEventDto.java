package org.example.oddventure.domain.match.dto.event;

import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record MatchStartEventDto(
        Long matchId,
        LocalDateTime StartTime
) {
    public static MatchStartEventDto from(Long matchId, LocalDateTime StartTime) {
        return MatchStartEventDto.builder()
                .matchId(matchId)
                .StartTime(StartTime)
                .build();
    }
}
