package org.example.oddventure.domain.admin.dto.response;

import java.math.BigDecimal;

public record PointAdjustResponse(
        Long userId,
        String username,
        BigDecimal adjustedAmount,
        BigDecimal finalBalance
) {
}
