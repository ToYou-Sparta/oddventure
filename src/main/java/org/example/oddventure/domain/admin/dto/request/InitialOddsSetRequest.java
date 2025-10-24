package org.example.oddventure.domain.admin.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record InitialOddsSetRequest(
        @NotNull(message = "A팀 배당률은 필수입니다.")
        @Positive(message = "A팀 배당률은 0보다 커야 합니다.")
        BigDecimal oddsA,

        @NotNull(message = "B팀 배당률은 필수입니다.")
        @Positive(message = "B팀 배당률은 0보다 커야 합니다.")
        BigDecimal oddsB
) {
}