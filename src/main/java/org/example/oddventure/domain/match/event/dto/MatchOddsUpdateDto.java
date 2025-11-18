package org.example.oddventure.domain.match.event.dto;

import java.math.BigDecimal;

public record MatchOddsUpdateDto(
        Long matchId,
        String selectedTeam,
        BigDecimal odds
) {
}
