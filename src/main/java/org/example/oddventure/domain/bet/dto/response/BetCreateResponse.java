package org.example.oddventure.domain.bet.dto.response;

import java.math.BigDecimal;
import lombok.Builder;
import org.example.oddventure.domain.bet.entity.Bet;
import org.example.oddventure.domain.bet.enums.SelectedTeam;

@Builder
public record BetCreateResponse(
        Long betId,
        Long userId,
        SelectedTeam selectedTeam,
        String selectedTeamName,
        BigDecimal betAmount,
        BigDecimal oddsAtBetting,
        BigDecimal userPointAfter
) {
    public static BetCreateResponse of(Bet bet, String selectedTeamName, BigDecimal userPointAfter) {
        return BetCreateResponse.builder()
                .betId(bet.getId())
                .userId(bet.getUser().getId())
                .selectedTeam(bet.getSelectedTeam())
                .selectedTeamName(selectedTeamName)
                .betAmount(bet.getBetAmount())
                .oddsAtBetting(bet.getOddsAtBetting())
                .userPointAfter(userPointAfter)
                .build();
    }
}
