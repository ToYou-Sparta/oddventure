package org.example.oddventure.domain.match.dto.event;

import java.math.BigDecimal;

public record MatchOddsUpdateDto(
        Long matchId,
        String selectedTeam,
        BigDecimal odds
) {
}
