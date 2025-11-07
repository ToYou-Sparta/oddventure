package org.example.oddventure.domain.ai.tools;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
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

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

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
        LocalDate today = LocalDate.now(KST);

        LocalDateTime startKst;
        LocalDateTime endKst;

        if (w.contains("오늘")) {
            startKst = today.atStartOfDay();
            endKst = today.plusDays(1).atStartOfDay();
        } else if (w.contains("내일")) {
            startKst = today.plusDays(1).atStartOfDay();
            endKst = today.plusDays(2).atStartOfDay();
        } else if (w.contains("어제")) {
            startKst = today.minusDays(1).atStartOfDay();
            endKst = today.atStartOfDay();
        } else if (w.contains("이번주")) {
            LocalDate mon = today.with(DayOfWeek.MONDAY);
            startKst = mon.atStartOfDay();
            endKst = mon.plusDays(7).atStartOfDay();
        } else if (w.contains("지난주")) {
            LocalDate monThis = today.with(DayOfWeek.MONDAY);
            LocalDate monLast = monThis.minusWeeks(1);
            startKst = monLast.atStartOfDay();
            endKst = monThis.atStartOfDay();
        } else if (w.contains("주말")) {
            LocalDate sat = today.with(DayOfWeek.SATURDAY);
            startKst = sat.atStartOfDay();
            endKst = sat.plusDays(2).atStartOfDay();
        } else {
            // 애매하면 오늘로 처리
            startKst = today.atStartOfDay();
            endKst = today.plusDays(1).atStartOfDay();
        }

        List<Match> matches = findMatchesByKstRange(startKst, endKst);
        System.out.println("조회된 경기 수: " + matches.size());

        if (matches.isEmpty()) {
            return ScheduleResponse.noMatch(w, startKst, endKst, "해당 기간에 등록된 일정이 없습니다.");
        }

        List<ScheduleItem> items = matches.stream()
                .map(ScheduleItem::of)
                .toList();

        return ScheduleResponse.of(items, w, startKst, endKst);
    }

    @Tool(
            name = "query_schedule_by_date",
            description = "한국어 날짜(M월 d일)를 숫자로 받아 해당 날짜의 일정을 조회한다."
    )
    public ScheduleResponse queryScheduleByDate(
            @ToolParam(description = "월(1~12)") int month,
            @ToolParam(description = "일(1~31)") int day
    ) {
        int year = LocalDate.now(KST).getYear();
        LocalDate targetDate = LocalDate.of(year, month, day);

        LocalDateTime startKst = targetDate.atStartOfDay();
        LocalDateTime endKst = targetDate.plusDays(1).atStartOfDay();

        List<Match> matches = findMatchesByKstRange(startKst, endKst);
        System.out.println("조회된 경기 수: " + matches.size());

        String label = month + "월 " + day + "일";
        if (matches.isEmpty()) {
            return ScheduleResponse.noMatch(label, startKst, endKst, "해당 날짜에 등록된 일정이 없습니다.");
        }

        List<ScheduleItem> items = matches.stream().map(ScheduleItem::of).toList();

        return ScheduleResponse.of(items, label, startKst, endKst);
    }

    public List<Match> findMatchesByKstRange(LocalDateTime startKst, LocalDateTime endKst) {
        ZoneId UTC = ZoneOffset.UTC;
        LocalDateTime startUtc = startKst.atZone(KST).withZoneSameInstant(UTC).toLocalDateTime();
        LocalDateTime endUtc = endKst.atZone(KST).withZoneSameInstant(UTC).toLocalDateTime();

        return matchRepository.findByStartTimeBetweenOrderByStartTimeAsc(startUtc, endUtc);
    }
}
