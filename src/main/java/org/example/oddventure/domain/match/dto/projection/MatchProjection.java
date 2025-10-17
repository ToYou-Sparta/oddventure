package org.example.oddventure.domain.match.dto.projection;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.example.oddventure.domain.match.entity.Match;
import org.example.oddventure.domain.match.enums.MatchStatus;
import org.example.oddventure.domain.match.enums.MatchWinner;

public record MatchProjection(
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

    public static MatchProjection from(Match match) {
        return new MatchProjection(
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
