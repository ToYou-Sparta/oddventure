package org.example.oddventure.domain.admin.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record MatchCreateRequest(

        @NotBlank(message = "경기 이름은 필수 입력 값입니다.")
        String matchName,

        @NotBlank(message = "팀 A의 이름은 필수 입력 값입니다.")
        String teamA,

        @NotBlank(message = "팀 B의 이름은 필수 입력 값입니다.")
        String teamB,

        @NotNull(message = "경기 시작 시간은 필수 입력 값입니다.")
        @Future(message = "경기 시작 시간은 현재보다 미래여야 합니다.")
        LocalDateTime startTime
) {
}
