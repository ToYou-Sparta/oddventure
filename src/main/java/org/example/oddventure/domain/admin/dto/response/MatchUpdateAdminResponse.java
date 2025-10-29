package org.example.oddventure.domain.admin.dto.response;

import java.time.LocalDateTime;
import lombok.Builder;
import org.example.oddventure.domain.match.entity.Match;
import org.example.oddventure.domain.match.enums.MatchStatus;

@Builder
public record MatchUpdateAdminResponse(
        Long matchId,
        String matchName,
        String teamA,
        String teamB,
        LocalDateTime startTime,
        MatchStatus status
) {
    public static MatchUpdateAdminResponse from(Match match) {
        return MatchUpdateAdminResponse.builder()
                .matchId(match.getId())
                .matchName(match.getMatchName())
                .teamA(match.getTeamA())
                .teamB(match.getTeamB())
                .startTime(match.getStartTime())
                .status(match.getStatus())
                .build();
    }
}