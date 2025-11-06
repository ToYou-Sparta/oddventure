package org.example.oddventure.domain.bet.dto;

import java.math.BigDecimal;
import lombok.Builder;

@Builder
public record PointEventDto(
        Long userId,
        BigDecimal betAmount
) {
    public static PointEventDto from(Long userId, BigDecimal betAmount) {
        return PointEventDto.builder()
                .userId(userId)
                .betAmount(betAmount)
                .build();
    }
}
