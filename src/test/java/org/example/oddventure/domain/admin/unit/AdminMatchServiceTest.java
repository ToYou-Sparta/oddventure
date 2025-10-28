package org.example.oddventure.domain.admin.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.Optional;
import org.example.oddventure.common.exception.GlobalException;
import org.example.oddventure.domain.admin.dto.request.MatchCreateRequest;
import org.example.oddventure.domain.admin.dto.request.MatchUpdateRequest;
import org.example.oddventure.domain.admin.dto.response.MatchAdminResponse;
import org.example.oddventure.domain.admin.service.AdminMatchService;
import org.example.oddventure.domain.match.entity.Match;
import org.example.oddventure.domain.match.enums.MatchStatus;
import org.example.oddventure.domain.match.repository.MatchRepository;
import org.example.oddventure.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class AdminMatchServiceTest {

    @InjectMocks
    private AdminMatchService adminMatchService;

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private UserRepository userRepository;

    @Test
    @DisplayName("매치 생성 성공")
    void createMatch_Success() {
        // given
        LocalDateTime startTime = LocalDateTime.now().plusDays(1);
        MatchCreateRequest request = new MatchCreateRequest("LCK", "T1", "Gen.G", startTime);

        Match match = Match.builder().teamA("T1").teamB("Gen.G").startTime(startTime).build();

        given(matchRepository.save(any(Match.class))).willReturn(match);

        // when
        MatchAdminResponse response = adminMatchService.createMatch(request);

        // then
        assertThat(response.teamA()).isEqualTo("T1");
        assertThat(response.teamB()).isEqualTo("Gen.G");
        verify(matchRepository).save(any(Match.class));
    }

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

        // when
        MatchAdminResponse response = adminMatchService.updateMatch(matchId, request);

        // then
        assertThat(response.teamA()).isEqualTo("DWG KIA");
        assertThat(response.startTime()).isEqualTo(newStartTime);
        assertThat(response.status()).isEqualTo(MatchStatus.ONGOING);
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
            adminMatchService.updateMatch(matchId, request);
        });
    }
}
