package org.example.oddventure.domain.match.dto.response;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.example.oddventure.domain.match.entity.Match;
import org.example.oddventure.domain.match.enums.MatchStatus;
import org.example.oddventure.domain.match.enums.MatchWinner;

public record MatchResponse(
        Long matchId,
        String matchName,
        String teamA,
        String teamB,
        BigDecimal totalAmountA,
        BigDecimal totalAmountB,
        LocalDateTime startTime,
        LocalDateTime endTime,
        MatchStatus status,
        MatchWinner winner,
        Long viewCount,
        LocalDateTime createdAt
) {

    public static MatchResponse from(Match match) {
        return new MatchResponse(
                match.getId(),
                match.getMatchName(),
                match.getTeamA(),
                match.getTeamB(),
                match.getTotalAmountA(),
                match.getTotalAmountB(),
                match.getStartTime(),
                match.getEndTime(),
                match.getStatus(),
                match.getWinner(),
                match.getViewCount(),
                match.getCreatedAt()
        );
    }
}
