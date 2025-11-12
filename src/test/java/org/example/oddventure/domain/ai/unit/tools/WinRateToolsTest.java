package org.example.oddventure.domain.ai.unit.tools;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import org.example.oddventure.domain.ai.dto.WinRateResponse;
import org.example.oddventure.domain.ai.tools.WinRateTools;
import org.example.oddventure.domain.match.repository.MatchRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class WinRateToolsTest {

    @Mock
    private MatchRepository matchRepository;

    @InjectMocks
    private WinRateTools winRateTools;

    @Test
    @DisplayName("단일 팀 승률을 계산한다")
    void analyzeWinningRate_singleTeam() {
        // given
        when(matchRepository.findByWinnerIsNotNull())
                .thenReturn(List.of("FaZe Clan", "FaZe Clan", "Team Vitality"));
        when(matchRepository.findByLoserIsNotNull())
                .thenReturn(List.of("Team Vitality", "G2 Esports", "FaZe Clan"));

        // when
        WinRateResponse response = winRateTools.analyzeWinningRate("FaZe Clan", null);

        // then
        assertThat(response).isNotNull();
        assertThat(response.result()).isTrue();
        assertThat(response.items()).hasSize(1);
        assertThat(response.items().get(0).team()).isEqualTo("FaZe Clan");
        assertThat(response.items().get(0).winningRate()).isEqualTo(67);
        assertThat(response.content()).contains("FaZe Clan의 누적 승률");
    }

    @Test
    @DisplayName("두 팀 비교 모드로 승률을 계산한다")
    void analyzeWinningRate_compareTwoTeams() {
        // given
        when(matchRepository.findByWinnerIsNotNull())
                .thenReturn(List.of("FaZe Clan", "FaZe Clan", "Team Vitality"));
        when(matchRepository.findByLoserIsNotNull())
                .thenReturn(List.of("Team Vitality", "G2 Esports", "FaZe Clan"));

        // when
        WinRateResponse response = winRateTools.analyzeWinningRate("FaZe Clan", "Team Vitality");

        // then
        assertThat(response).isNotNull();
        assertThat(response.result()).isTrue();
        assertThat(response.items()).hasSize(2);
        assertThat(response.compareTarget()).isEqualTo("FaZe Clan vs Team Vitality");
        assertThat(response.content()).contains("FaZe Clan").contains("Team Vitality");
    }

    @Test
    @DisplayName("팀명이 모두 null이면 에러 응답을 반환한다")
    void analyzeWinningRate_nullTeamNames() {
        // given
        when(matchRepository.findByWinnerIsNotNull()).thenReturn(List.of());
        when(matchRepository.findByLoserIsNotNull()).thenReturn(List.of());

        // when
        WinRateResponse response = winRateTools.analyzeWinningRate(null, null);

        // then
        assertThat(response).isNotNull();
        assertThat(response.result()).isFalse();
        assertThat(response.items()).isEmpty();
        assertThat(response.message()).contains("팀명을 알려주세요");
    }
}
