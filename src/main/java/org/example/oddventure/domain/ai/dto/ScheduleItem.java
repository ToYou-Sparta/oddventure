package org.example.oddventure.domain.ai.dto;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import org.example.oddventure.domain.match.entity.Match;

public record ScheduleItem(
        Long id,
        String startTimeKst,
        String matchName,
        String teamA,
        String teamB
) {


    public static ScheduleItem of(Match match) {
        ZonedDateTime kst = ZonedDateTime.of(match.getStartTime(), ZoneOffset.UTC)
                .withZoneSameInstant(ZoneId.of("Asia/Seoul"));
        return new ScheduleItem(
                match.getId(),
                kst.toString(),
                match.getMatchName(),
                match.getTeamA(),
                match.getTeamB()
        );
    }
}
