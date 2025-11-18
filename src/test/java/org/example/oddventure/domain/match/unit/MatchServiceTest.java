package org.example.oddventure.domain.match.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.example.oddventure.common.exception.GlobalException;
import org.example.oddventure.domain.admin.dto.request.MatchUpdateRequest;
import org.example.oddventure.domain.admin.dto.response.MatchUpdateAdminResponse;
import org.example.oddventure.domain.grid.dto.MatchScheduleDto;
import org.example.oddventure.domain.hotKeywords.service.HotKeywordsService;
import org.example.oddventure.domain.match.dto.MatchCreateDto;
import org.example.oddventure.domain.match.dto.projection.MatchProjection;
import org.example.oddventure.domain.match.dto.request.MatchSearchCondition;
import org.example.oddventure.domain.match.dto.response.MatchResponse;
import org.example.oddventure.domain.match.entity.Match;
import org.example.oddventure.domain.match.enums.MatchStatus;
import org.example.oddventure.domain.match.event.MatchEventProducer;
import org.example.oddventure.domain.match.event.MatchNotificationProducer;
import org.example.oddventure.domain.match.event.dto.MatchInfoUpdateDto;
import org.example.oddventure.domain.match.event.MatchEsSyncPublisher;
import org.example.oddventure.domain.match.exception.MatchErrorCode;
import org.example.oddventure.domain.match.exception.MatchException;
import org.example.oddventure.domain.match.repository.MatchJdbcRepository;
import org.example.oddventure.domain.match.repository.MatchRepository;
import org.example.oddventure.domain.match.service.MatchSearchService;
import org.example.oddventure.domain.match.service.MatchService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
class MatchServiceTest {

    @InjectMocks
    private MatchService matchService;

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private MatchEventProducer matchEventProducer;

    @Mock
    private MatchNotificationProducer matchNotificationProducer;

    @Mock
    private HotKeywordsService hotKeywordsService;

    @Mock
    private MatchJdbcRepository matchJdbcRepository;

    @Mock
    private MatchSearchService matchSearchService;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private MatchEsSyncPublisher esSyncPublisher;

    @Test
    @DisplayName("매치 생성 성공")
    void createMatch_Success() {
        // given
        LocalDateTime startTime = LocalDateTime.now().plusDays(1);
        List<MatchScheduleDto> request = new ArrayList<>();
        request.add(new MatchScheduleDto(1L, "LCK", "T1", "Gen.G", startTime));

        given(matchRepository.findExistingFetchIds(anyList())).willReturn(List.of());
        doNothing().when(matchJdbcRepository).saveAllMatches(anyList());
        doNothing().when(esSyncPublisher).publishMatchCreated(any());

        // when
        List<MatchCreateDto> response = matchService.createMatch(request);

        // then
        assertThat(response.get(0).fetchId()).isEqualTo(1L);
        verify(matchJdbcRepository).saveAllMatches(anyList());
        verify(esSyncPublisher).publishMatchCreated(any());
    }

    @Test
    @DisplayName("경기 목록 조회 성공")
    void getMatches_success() {
        // given
        Match match1 = Match.builder()
                .matchName("LCK")
                .teamA("T1")
                .teamB("GEN.G")
                .startTime(LocalDateTime.now().plusDays(1))
                .build();

        Match match2 = Match.builder()
                .matchName("LCK")
                .teamA("KT")
                .teamB("DRX")
                .startTime(LocalDateTime.now().plusDays(2))
                .build();

        Pageable pageable = PageRequest.of(0, 10, Sort.by("startTime").ascending());
        Page<Match> matches = new PageImpl<>(List.of(match1, match2));
        when(matchRepository.findAll(pageable)).thenReturn(matches);

        // when
        Page<MatchResponse> result = matchService.getMatches(pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).startTime()).isBefore(
                result.getContent().get(1).startTime());
        assertThat(result.getContent().get(0).teamA()).isEqualTo("T1");
        assertThat(result.getContent().get(1).teamB()).isEqualTo("DRX");
    }

    @Test
    @DisplayName("경기 검색 성공")
    void searchMatches_success() {
        // given
        MatchSearchCondition condition = new MatchSearchCondition("T", null, null);
        Pageable pageable = PageRequest.of(0, 10);

        Match match1 = Match.builder()
                .matchName("LCK")
                .teamA("T1")
                .teamB("GEN.G")
                .startTime(LocalDateTime.now().plusDays(1))
                .build();

        Match match2 = Match.builder()
                .matchName("LCK")
                .teamA("KT")
                .teamB("DRX")
                .startTime(LocalDateTime.now().plusDays(2))
                .build();

        MatchProjection response1 = MatchProjection.from(match1);
        MatchProjection response2 = MatchProjection.from(match2);
        Page<MatchProjection> matches = new PageImpl<>(List.of(response1, response2), pageable, 2);

        doNothing().when(hotKeywordsService).incrementSearchScore(condition.keyword());
        when(matchRepository.searchByCondition(condition, pageable)).thenReturn(matches);

        // when
        Page<MatchResponse> result = matchService.searchMatches(condition, pageable);

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).teamA()).isEqualTo("T1");
        assertThat(result.getContent().get(1).teamA()).isEqualTo("KT");
    }

    @Test
    @DisplayName("매치 상태값 변경 성공")
    void updateStatus_success() {
        //given
        Long fetchId = 1L;
        MatchStatus status = MatchStatus.ONGOING;

        Match match = Match.builder()
                .matchName("LCK")
                .teamA("T1")
                .teamB("GEN.G")
                .startTime(LocalDateTime.now().plusDays(1))
                .build();

        when(matchRepository.findByFetchId(fetchId)).thenReturn(Optional.of(match));

        //when
        matchService.updateStatus(fetchId, status);

        //then
        assertThat(match.getStatus()).isEqualTo(status);
        verify(matchNotificationProducer).sendMatchStatusChanged(any(MatchInfoUpdateDto.class));
    }

    @Nested
    @DisplayName("매치 수정")
    class UpdateMatch {
        @Test
        @DisplayName("매치 정보 수정 성공")
        void updateMatch_Success() {
            // given
            Long matchId = 1L;
            LocalDateTime newStartTime = LocalDateTime.now().plusHours(5);
            MatchUpdateRequest request = new MatchUpdateRequest(
                    "LCK", "DWG KIA", "T1", newStartTime, MatchStatus.ONGOING
            );

            Match existingMatch = Match.builder()
                    .teamA("DK")
                    .teamB("T1")
                    .startTime(LocalDateTime.now().plusHours(3))
                    .build();

            given(matchRepository.findById(matchId)).willReturn(Optional.of(existingMatch));
            doNothing().when(esSyncPublisher).publishMatchUpdated(anyLong());

            // when
            MatchUpdateAdminResponse response = matchService.updateMatch(matchId, request);

            // then
            assertThat(response.teamA()).isEqualTo("DWG KIA");
            assertThat(response.startTime()).isEqualTo(newStartTime);
            assertThat(response.status()).isEqualTo(MatchStatus.ONGOING);
            verify(esSyncPublisher).publishMatchUpdated(matchId);
        }

        @Test
        @DisplayName("매치 수정 실패 - 존재하지 않는 매치")
        void updateMatch_Fail_MatchNotFound() {
            // given
            Long matchId = 999L;
            MatchUpdateRequest request = new MatchUpdateRequest(
                    "LCK", "DWG KIA", "T1", LocalDateTime.now().plusHours(5), MatchStatus.ONGOING
            );

            given(matchRepository.findById(matchId)).willReturn(Optional.empty());

            // when & then
            assertThrows(GlobalException.class, () -> {
                matchService.updateMatch(matchId, request);
            });
        }
    }

    @Nested
    @DisplayName("경기 상세 조회 (캐시 적용)")
    class GetMatch {
        @Test
        @DisplayName("경기 상세 조회 성공")
        void getMatch_success() {
            // given
            Long matchId = 1L;
            Match match = Match.builder()
                    .matchName("LCK")
                    .teamA("T1")
                    .teamB("GEN.G")
                    .startTime(LocalDateTime.now().plusDays(1))
                    .build();

            when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));

            // when
            MatchResponse result = matchService.getMatch(matchId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.teamA()).isEqualTo("T1");
            verify(matchRepository).findById(matchId); // DB SELECT만 호출됨
        }

        @Test
        @DisplayName("경기 상세 조회 실패 - findById 실패 시 MatchException 발생")
        void getMatch_fail_findById() {
            // given
            Long matchId = 1L;
            when(matchRepository.findById(matchId)).thenReturn(Optional.empty());

            // when
            MatchException exception = assertThrows(MatchException.class, () -> matchService.getMatch(matchId));

            // then
            assertThat(exception.getMessage()).isEqualTo(MatchErrorCode.MATCH_NOT_FOUND.getMessage());
        }
    }

    @Nested
    @DisplayName("조회수 증가 (Redis INCR)")
    class IncrementViewCount {
        @Test
        @DisplayName("조회수 증가 성공 - Redis INCR 호출")
        void incrementViewCount_success() {
            // given
            Long matchId = 1L;
            String expectedKey = "match:viewcount:" + matchId;
            given(redisTemplate.opsForValue()).willReturn(valueOperations);
            given(valueOperations.increment(expectedKey)).willReturn(1L);

            // when
            matchService.incrementViewCount(matchId);

            // then
            verify(matchRepository, never()).existsById(anyLong());

            // Redis INCR이 1번 호출되었는지 검증
            verify(redisTemplate.opsForValue()).increment(expectedKey);
        }
    }

    @Nested
    @DisplayName("조회수 DB 동기화 (Scheduler)")
    class UpdateViewCount {
        @Test
        @DisplayName("조회수 DB 동기화 성공")
        void updateViewCount_success() {
            // given
            Long matchId = 1L;
            Long viewCount = 120L;
            given(matchRepository.updateViewCount(matchId, viewCount)).willReturn(1);

            // when
            matchService.updateViewCount(matchId, viewCount);

            // then
            verify(matchRepository).updateViewCount(matchId, viewCount);
        }

        @Test
        @DisplayName("조회수 DB 동기화 - 대상 없음 (WARN 로그)")
        void updateViewCount_fail_notFound() {
            // given
            Long matchId = 999L;
            Long viewCount = 120L;
            given(matchRepository.updateViewCount(matchId, viewCount)).willReturn(0);

            // when
            matchService.updateViewCount(matchId, viewCount);

            // then
            verify(matchRepository).updateViewCount(matchId, viewCount);
        }
    }

    @Test
    @DisplayName("매치 상태값 변경 성공")
    void updateStatus_success() {
        //given
        Long fetchId = 1L;
        MatchStatus status = MatchStatus.ONGOING;

        Match match = Match.builder()
                .matchName("LCK")
                .teamA("T1")
                .teamB("GEN.G")
                .startTime(LocalDateTime.now().plusDays(1))
                .build();

        when(matchRepository.findByFetchId(fetchId)).thenReturn(Optional.of(match));
        doNothing().when(esSyncPublisher).publishMatchUpdated(any());

        //when
        matchService.updateStatus(fetchId, status);

        //then
        assertThat(match.getStatus()).isEqualTo(status);
        verify(esSyncPublisher).publishMatchUpdated(any());
    }
}
