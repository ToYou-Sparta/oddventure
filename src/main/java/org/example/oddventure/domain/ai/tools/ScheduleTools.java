package org.example.oddventure.domain.ai.tools;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.oddventure.domain.ai.dto.ScheduleItem;
import org.example.oddventure.domain.ai.dto.ScheduleResponse;
import org.example.oddventure.domain.match.entity.Match;
import org.example.oddventure.domain.match.repository.MatchRepository;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ScheduleTools {

    private final MatchRepository matchRepository;

    @Tool(
            name = "query_schedule",
            description = "날짜 표현(오늘/내일/어제/이번주/지난주/주말)을 해석해 해당 기간의 경기 일정을 조회한다."
    )
    public ScheduleResponse querySchedule(
            @ToolParam(description = "예: 오늘/내일/어제/이번주/지난주/주말", required = false)
            String when
    ) {
        String w = (when == null || when.isBlank()) ? "오늘" : when.trim();
        LocalDate today = LocalDate.now();

        LocalDateTime start;
        LocalDateTime end;

        if (w.contains("오늘")) {
            start = today.atStartOfDay();
            end = today.plusDays(1).atStartOfDay();
        } else if (w.contains("내일")) {
            start = today.plusDays(1).atStartOfDay();
            end = today.plusDays(2).atStartOfDay();
        } else if (w.contains("어제")) {
            start = today.minusDays(1).atStartOfDay();
            end = today.atStartOfDay();
        } else if (w.contains("이번주")) {
            LocalDate mon = today.with(DayOfWeek.MONDAY);
            start = mon.atStartOfDay();
            end = mon.plusDays(7).atStartOfDay();
        } else if (w.contains("지난주")) {
            LocalDate monThis = today.with(DayOfWeek.MONDAY);
            LocalDate monLast = monThis.minusWeeks(1);
            start = monLast.atStartOfDay();
            end = monThis.atStartOfDay();
        } else if (w.contains("주말")) {
            LocalDate sat = today.with(DayOfWeek.SATURDAY);
            start = sat.atStartOfDay();
            end = sat.plusDays(2).atStartOfDay();
        } else {
            // 애매하면 오늘로 처리
            start = today.atStartOfDay();
            end = today.plusDays(1).atStartOfDay();
        }

        List<Match> matches = matchRepository.findByStartTimeBetweenOrderByStartTimeAsc(start, end);

        if (matches.isEmpty()) {
            return ScheduleResponse.noMatch(w, start, end, "해당 기간에 등록된 일정이 없습니다.");
        }

        List<ScheduleItem> items = matches.stream()
                .map(ScheduleItem::of)
                .toList();

        return ScheduleResponse.of(items, w, start, end);
    }
}
