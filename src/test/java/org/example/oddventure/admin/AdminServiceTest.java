package org.example.oddventure.admin;

import org.example.oddventure.common.exception.GlobalException;
import org.example.oddventure.domain.admin.dto.request.MatchCreateRequest;
import org.example.oddventure.domain.admin.dto.request.MatchUpdateRequest;
import org.example.oddventure.domain.admin.dto.response.MatchAdminResponse;
import org.example.oddventure.domain.admin.service.AdminService;
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
import java.time.LocalDateTime;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import org.example.oddventure.domain.admin.dto.response.UserAdminResponse;
import org.example.oddventure.domain.user.entity.User;
import org.example.oddventure.domain.user.enums.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @InjectMocks
    private AdminService adminService;

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private UserRepository userRepository;

    @Test
    @DisplayName("매치 생성 성공")
    void createMatch_Success() {
        // given
        LocalDateTime startTime = LocalDateTime.now().plusDays(1);
        MatchCreateRequest request = new MatchCreateRequest("T1", "Gen.G", startTime);

        Match match = Match.builder().teamA("T1").teamB("Gen.G").startTime(startTime).build();

        given(matchRepository.save(any(Match.class))).willReturn(match);

        // when
        MatchAdminResponse response = adminService.createMatch(request);

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
                "DWG KIA", "T1", newStartTime, MatchStatus.ONGOING
        );

        Match existingMatch = Match.builder()
                .teamA("DK")
                .teamB("T1")
                .startTime(LocalDateTime.now().plusHours(3))
                .build();

        given(matchRepository.findById(matchId)).willReturn(Optional.of(existingMatch));

        // when
        MatchAdminResponse response = adminService.updateMatch(matchId, request);

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
                "DWG KIA", "T1", LocalDateTime.now().plusHours(5), MatchStatus.ONGOING
        );

        given(matchRepository.findById(matchId)).willReturn(Optional.empty());

        // when & then
        assertThrows(GlobalException.class, () -> {
            adminService.updateMatch(matchId, request);
        });
    }

    @Test
    @DisplayName("검색 조건 포함 사용자 목록 조회 성공")
    void getAllUsers_WithFilters_Success() {
        // given
        String email = "test";
        String username = "user";
        Pageable pageable = PageRequest.of(0, 10);

        User user1 = User.builder()
                .email("test1@test.com")
                .username("testuser1")
                .password("password")
                .userRole(UserRole.ROLE_USER)
                .build();

        Page<User> mockUserPage = new PageImpl<>(List.of(user1), pageable, 1);

        given(userRepository.findBySearchConditions(email, username, pageable)).willReturn(mockUserPage);

        // when
        Page<UserAdminResponse> result = adminService.getAllUsers(email, username, pageable);

        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).email()).isEqualTo("test1@test.com");
        assertThat(result.getContent().get(0).point().intValue()).isEqualTo(1000); // 기본 1000 포인트 확인
    }

    @Test
    @DisplayName("검색 조건 없이 사용자 목록 조회 성공")
    void getAllUsers_NoFilters_Success() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        User user1 = User.builder().email("test1@test.com").username("user1").password("p").userRole(UserRole.ROLE_USER).build();
        User user2 = User.builder().email("test2@test.com").username("user2").password("p").userRole(UserRole.ROLE_USER).build();

        Page<User> mockUserPage = new PageImpl<>(List.of(user1, user2), pageable, 2);

        // 검색 조건이 null로 넘어오는 경우를 테스트
        given(userRepository.findBySearchConditions(null, null, pageable)).willReturn(mockUserPage);

        // when
        Page<UserAdminResponse> result = adminService.getAllUsers(null, null, pageable);

        // then
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).hasSize(2);
    }
}