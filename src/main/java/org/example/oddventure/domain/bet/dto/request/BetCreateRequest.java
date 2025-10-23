package org.example.oddventure.domain.bet.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import org.example.oddventure.domain.bet.entity.Bet;
import org.example.oddventure.domain.bet.enums.SelectedTeam;
import org.example.oddventure.domain.match.entity.Match;
import org.example.oddventure.domain.user.entity.User;

public record BetCreateRequest(

        @NotNull(message = "경기 ID는 필수 입력 값입니다.")
        Long matchId,

        @NotNull(message = "베팅할 팀을 선택해야 합니다.")
        SelectedTeam selectedTeam,

        @Positive(message = "베팅 금액은 0보다 커야 합니다.")
        Long betAmount
) {
    public Bet toEntity(User user, Match match, BigDecimal odds) {
        return Bet.builder()
                .user(user)
                .match(match)
                .selectedTeam(selectedTeam)
                .betAmount(BigDecimal.valueOf(betAmount))
                .oddsAtBetting(odds)
                .build();
    }
}