package org.example.oddventure.domain.match.event.dto;

import java.time.LocalDateTime;
import org.example.oddventure.domain.match.entity.Match;
import org.example.oddventure.domain.match.enums.MatchStatus;

public record MatchInfoUpdateDto(
        Long matchId,
        String matchName,
        String teamA,
        String teamB,
        LocalDateTime startTime,
        MatchStatus status
) {
    public static MatchInfoUpdateDto of(Match match) {
        return new MatchInfoUpdateDto(
                match.getId(),
                match.getMatchName(),
                match.getTeamA(),
                match.getTeamB(),
                match.getStartTime(),
                match.getStatus()
        );
    }
}
