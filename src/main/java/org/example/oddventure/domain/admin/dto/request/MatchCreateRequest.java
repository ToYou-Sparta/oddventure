package org.example.oddventure.domain.admin.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record MatchCreateRequest(
        @NotBlank String matchName,
        @NotBlank String teamA,
        @NotBlank String teamB,
        @NotNull @Future(message = "경기 시작 시간은 현재보다 미래여야 합니다.") LocalDateTime startTime
) {
}
