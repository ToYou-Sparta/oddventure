package org.example.oddventure.domain.admin.dto.response;

import org.example.oddventure.domain.match.entity.Match;
import org.example.oddventure.domain.match.enums.MatchStatus;
import java.time.LocalDateTime;

public record MatchAdminResponse(
        Long matchId,
        String teamA,
        String teamB,
        LocalDateTime startTime,
        MatchStatus status
) {
    public static MatchAdminResponse fromEntity(Match match) {
        return new MatchAdminResponse(
                match.getId(),
                match.getTeamA(),
                match.getTeamB(),
                match.getStartTime(),
                match.getStatus()
        );
    }
}