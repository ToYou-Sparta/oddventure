package org.example.oddventure.domain.ai.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ScheduleResponse(
        List<ScheduleItem> matches,
        String when,
        String start,
        String end,
        String message
) {
    public static ScheduleResponse of(
            List<ScheduleItem> matches,
            String when,
            LocalDateTime start,
            LocalDateTime end
    ) {
        return new ScheduleResponse(matches, when, start.toString(), end.toString(), null);
    }

    public static ScheduleResponse noMatch(
            String when,
            LocalDateTime start,
            LocalDateTime end,
            String message
    ) {
        return new ScheduleResponse(List.of(), when, start.toString(), end.toString(), message);
    }
}
