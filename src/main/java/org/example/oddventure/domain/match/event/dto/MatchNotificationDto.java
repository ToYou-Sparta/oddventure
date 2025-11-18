package org.example.oddventure.domain.match.event.dto;

public record MatchNotificationDto(
        Long matchId,
        String category,
        Object payload
) {
}
