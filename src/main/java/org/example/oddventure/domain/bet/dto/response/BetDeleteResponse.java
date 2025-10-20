package org.example.oddventure.domain.bet.dto.response;

import java.math.BigDecimal;
import lombok.Builder;
import org.example.oddventure.domain.bet.entity.Bet;
import org.example.oddventure.domain.user.entity.User;

@Builder
public record BetDeleteResponse(
        Long betId,
        BigDecimal refundAmount,
        BigDecimal userPointAfter
) {
    public static BetDeleteResponse of(Bet bet, User user) {
        return BetDeleteResponse.builder()
                .betId(bet.getId())
                .refundAmount(bet.getBetAmount())
                .userPointAfter(user.getPoint())
                .build();
    }
}
