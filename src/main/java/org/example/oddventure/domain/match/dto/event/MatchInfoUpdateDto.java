package org.example.oddventure.domain.match.dto.event;

import java.time.LocalDateTime;
import org.example.oddventure.domain.match.enums.MatchStatus;

public record MatchInfoUpdateDto(
        Long matchId,
        String matchName,
        String teamA,
        String teamB,
        LocalDateTime startTime,
        MatchStatus status
) {
}
