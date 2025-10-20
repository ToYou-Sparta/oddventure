package org.example.oddventure.domain.bet.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;
import org.example.oddventure.domain.bet.entity.Bet;
import org.example.oddventure.domain.bet.enums.SelectedTeam;
import org.example.oddventure.domain.match.dto.response.MatchBetResponse;

@Builder
public record BetResponse(
        Long betId,
        MatchBetResponse matchBetResponse,
        SelectedTeam selectedTeam,
        BigDecimal betAmount,
        BigDecimal oddsAtBetting,
        boolean isWin,
        LocalDateTime createdAt
) {
    public static BetResponse from(Bet bet) {
        MatchBetResponse response = MatchBetResponse.from(bet.getMatch());

        return BetResponse.builder()
                .betId(bet.getId())
                .matchBetResponse(response)
                .selectedTeam(bet.getSelectedTeam())
                .betAmount(bet.getBetAmount())
                .oddsAtBetting(bet.getOddsAtBetting())
                .isWin(bet.isWin())
                .createdAt(bet.getCreatedAt())
                .build();
    }
}
