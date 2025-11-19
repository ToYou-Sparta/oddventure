package org.example.oddventure.domain.match.dto.response;

import java.time.LocalDateTime;
import lombok.Builder;
import org.example.oddventure.domain.match.entity.Match;
import org.example.oddventure.domain.match.enums.MatchStatus;

@Builder
public record MatchBetResponse(
        Long matchId,
        String teamA,
        String teamB,
        LocalDateTime startTime,
        MatchStatus status
) {
    public static MatchBetResponse from(Match match) {
        return MatchBetResponse.builder()
                .matchId(match.getId())
                .teamA(match.getTeamA())
                .teamB(match.getTeamB())
                .startTime(match.getStartTime())
                .status(match.getStatus())
                .build();
    }
}
