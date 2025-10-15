package org.example.oddventure.domain.match.dto.response;


import org.example.oddventure.domain.match.entity.Match;
import org.example.oddventure.domain.match.enums.MatchStatus;
import org.example.oddventure.domain.match.enums.MatchWinner;

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

    public static MatchResponse from(Match match) {
        return new MatchResponse(
                match.getId(),
                match.getTeamA(),
                match.getTeamB(),
                match.getTotalAmountA(),
                match.getTotalAmountB(),
                match.getStartTime(),
                match.getEndTime(),
                match.getStatus(),
                match.getWinner(),
                match.getCreatedAt()
        );
    }
}
