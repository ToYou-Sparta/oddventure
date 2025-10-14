package org.example.oddventure.match.dto.response;


import org.example.oddventure.match.enums.MatchStatus;
import org.example.oddventure.match.enums.MatchWinner;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MatchResponse(
        Long matchId,
        String teamA,
        String teamB,
        BigDecimal totalAmountA,
        BigDecimal totalAmountB,
        LocalDateTime startTime,
        LocalDateTime endTime,
        MatchStatus status,
        MatchWinner winner,
        LocalDateTime createdAt
) {
}
