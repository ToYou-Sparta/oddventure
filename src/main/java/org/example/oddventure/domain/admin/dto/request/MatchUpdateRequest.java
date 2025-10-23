package org.example.oddventure.domain.admin.dto.request;

import jakarta.validation.constraints.Future;
import java.time.LocalDateTime;
import org.example.oddventure.domain.match.enums.MatchStatus;

public record MatchUpdateRequest(

        String matchName,

        String teamA,

        String teamB,

        @Future(message = "경기 시작 시간은 현재보다 미래여야 합니다.")
        LocalDateTime startTime,

        MatchStatus status
) {
}