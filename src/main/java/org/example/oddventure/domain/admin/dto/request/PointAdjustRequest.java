package org.example.oddventure.domain.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record PointAdjustRequest(

        @NotNull(message = "조정 금액은 필수 입력 값입니다.")
        @Positive(message = "조정 금액은 0보다 커야 합니다.")
        BigDecimal amount,

        @NotBlank(message = "포인트 조정 사유는 필수 입력 값입니다.")
        String reason
) {
}