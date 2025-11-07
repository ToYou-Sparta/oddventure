package org.example.oddventure.domain.ai.dto;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import org.example.oddventure.domain.match.entity.Match;

public record ScheduleItem(
        Long id,
        ZonedDateTime startTimeKst,
        String matchName,
        String teamA,
        String teamB
) {
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    
    public static ScheduleItem of(Match match) {
        return new ScheduleItem(
                match.getId(),
                match.getStartTime().atZone(KST),
                match.getMatchName(),
                match.getTeamA(),
                match.getTeamB()
        );
    }
}
