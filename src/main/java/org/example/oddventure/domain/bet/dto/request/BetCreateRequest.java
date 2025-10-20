package org.example.oddventure.domain.bet.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import org.example.oddventure.domain.bet.entity.Bet;
import org.example.oddventure.domain.bet.enums.SelectedTeam;
import org.example.oddventure.domain.match.entity.Match;
import org.example.oddventure.domain.user.entity.User;

public record BetCreateRequest(
        @NotNull
        Long matchId,

        @NotNull
        SelectedTeam selectedTeam,

        @NotNull
        @Min(value = 1, message = "베팅 금액은 0보다 커야 합니다.")
        Long betAmount
) {
    public Bet toEntity(User user, Match match, BigDecimal odds) {
        return Bet.builder()
                .user(user)
                .match(match)
                .selectedTeam(selectedTeam)
                .betAmount(BigDecimal.valueOf(betAmount))
                .oddsAtBetting(odds)
                .isWin(false)
                .build();
    }
}
