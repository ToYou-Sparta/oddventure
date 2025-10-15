package org.example.oddventure.match.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import org.example.oddventure.domain.match.dto.response.MatchResponse;
import org.example.oddventure.domain.match.entity.Match;
import org.example.oddventure.domain.match.repository.MatchRepository;
import org.example.oddventure.domain.match.service.MatchService;
import org.junit.jupiter.api.DisplayName;
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

@ExtendWith(MockitoExtension.class)
class MatchServiceTest {

    @InjectMocks
    private MatchService matchService;

    @Mock
    private MatchRepository matchRepository;

    @Test
    @DisplayName("경기 목록 조회 성공")
    void getMatches() {

        // given
        Match match1 = Match.builder()
                .teamA("T1")
                .teamB("GEN.G")
                .startTime(LocalDateTime.now().plusDays(1))
                .build();

        Match match2 = Match.builder()
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
}
