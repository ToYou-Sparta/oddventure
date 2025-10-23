package org.example.oddventure.domain.admin.dto.response;

import java.time.LocalDateTime;
import org.example.oddventure.domain.match.entity.Match;
import org.example.oddventure.domain.match.enums.MatchStatus;

public record MatchAdminResponse(
        Long matchId,
        String matchName,
        String teamA,
        String teamB,
        LocalDateTime startTime,
        MatchStatus status
) {
    public static MatchAdminResponse from(Match match) {
        return new MatchAdminResponse(
                match.getId(),
                match.getMatchName(),
                match.getTeamA(),
                match.getTeamB(),
                match.getStartTime(),
                match.getStatus()
        );
    }
}