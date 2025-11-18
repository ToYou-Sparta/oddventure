package org.example.oddventure.domain.match.dto.response;


import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.example.oddventure.domain.match.dto.projection.MatchProjection;
import org.example.oddventure.domain.match.entity.Match;
import org.example.oddventure.domain.match.enums.MatchStatus;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.CLASS,
        include = JsonTypeInfo.As.PROPERTY,
        property = "@class"
)

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
        String winner,
        String loser,
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
                match.getLoser(),
                match.getViewCount(),
                match.getCreatedAt()
        );
    }

    public static MatchResponse of(MatchProjection projection) {
        return new MatchResponse(
                projection.matchId(),
                projection.matchName(),
                projection.teamA(),
                projection.teamB(),
                projection.totalAmountA(),
                projection.totalAmountB(),
                projection.startTime(),
                projection.endTime(),
                projection.status(),
                projection.winner(),
                projection.loser(),
                projection.viewCount(),
                projection.createdAt()
        );
    }
}