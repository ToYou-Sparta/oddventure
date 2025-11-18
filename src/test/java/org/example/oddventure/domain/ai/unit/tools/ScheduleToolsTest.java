package org.example.oddventure.domain.ai.unit.tools;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import org.example.oddventure.domain.ai.dto.ScheduleResponse;
import org.example.oddventure.domain.ai.tools.ScheduleTools;
import org.example.oddventure.domain.match.entity.Match;
import org.example.oddventure.domain.match.repository.MatchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ScheduleToolsTest {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private LocalDate today;
    private Match match;
    @Mock
    private MatchRepository matchRepository;
    @InjectMocks
    private ScheduleTools scheduleTools;

    @BeforeEach
    void setUp() {
        today = LocalDate.now(KST);

        match = Match.builder()
                .matchName("IEM Katowice 2025")
                .teamA("FaZe Clan")
                .teamB("G2 Esports")
                .startTime(today.atStartOfDay().plusHours(12))
                .build();
    }

    @Test
    @DisplayName("일정이 있으면 정상 응답을 반환한다")
    void querySchedule_success() {
        // given
        when(matchRepository.findByStartTimeBetweenOrderByStartTimeAsc(any(LocalDateTime.class),
                any(LocalDateTime.class))).thenReturn(List.of(match));

        // when
        ScheduleResponse response = scheduleTools.querySchedule("오늘");

        // then
        assertThat(response).isNotNull();
        assertThat(response.matches()).hasSize(1);
        assertThat(response.matches().get(0).teamB()).isEqualTo("G2 Esports");
        assertThat(response.when()).isEqualTo("오늘");
        assertThat(response.message()).isNull();
    }

    @Test
    @DisplayName("일정이 없으면 noMatch 응답을 반환한다")
    void querySchedule_fail() {
        // given
        when(matchRepository.findByStartTimeBetweenOrderByStartTimeAsc(any(LocalDateTime.class),
                any(LocalDateTime.class))).thenReturn(List.of());

        // when
        ScheduleResponse response = scheduleTools.querySchedule("오늘");

        // then
        assertThat(response).isNotNull();
        assertThat(response.matches()).isEmpty();
        assertThat(response.message()).contains("등록된 일정이 없습니다");
        assertThat(response.when()).isEqualTo("오늘");
    }

    @Test
    @DisplayName("M월 d일 요청 시 해당 날짜의 일정을 조회한다")
    void queryScheduleByDate_success() {
        // given
        when(matchRepository.findByStartTimeBetweenOrderByStartTimeAsc(any(LocalDateTime.class),
                any(LocalDateTime.class))).thenReturn(List.of(match));

        // when
        ScheduleResponse response = scheduleTools.queryScheduleByDate(today.getMonthValue(), today.getDayOfMonth());

        // then
        assertThat(response).isNotNull();
        assertThat(response.matches()).hasSize(1);
        assertThat(response.matches().get(0).teamA()).isEqualTo("FaZe Clan");
        assertThat(response.when()).contains("월").contains("일");
        assertThat(response.message()).isNull();
    }

    @Test
    @DisplayName("KST(사용자 입력) 시간대를 UTC(DB 저장)로 변환하여 조회한다")
    void querySchedule_kstToUtc() {
        // given
        when(matchRepository.findByStartTimeBetweenOrderByStartTimeAsc(any(LocalDateTime.class),
                any(LocalDateTime.class))).thenReturn(List.of(match));

        // when
        scheduleTools.querySchedule("오늘");

        // then
        ArgumentCaptor<LocalDateTime> startCaptor = forClass(LocalDateTime.class);

        verify(matchRepository).findByStartTimeBetweenOrderByStartTimeAsc(startCaptor.capture(), any());
        LocalDateTime startUtc = startCaptor.getValue();

        LocalDateTime startKst = today.atStartOfDay();
        LocalDateTime expectedUtc = startKst
                .atZone(KST)
                .withZoneSameInstant(ZoneOffset.UTC)
                .toLocalDateTime();

        assertThat(startUtc).isEqualTo(expectedUtc);
    }
}
