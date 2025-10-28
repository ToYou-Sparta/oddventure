package org.example.oddventure.domain.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.example.oddventure.common.exception.GlobalException;
import org.example.oddventure.domain.admin.dto.request.PointAdjustRequest;
import org.example.oddventure.domain.admin.dto.response.PointAdjustResponse;
import org.example.oddventure.domain.admin.dto.response.UserAdminResponse;
import org.example.oddventure.domain.admin.service.AdminUserService;
import org.example.oddventure.domain.match.repository.MatchRepository;
import org.example.oddventure.domain.user.entity.User;
import org.example.oddventure.domain.user.repository.UserRepository;
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

@ExtendWith(MockitoExtension.class)
public class AdminUserServiceTest {

    @InjectMocks
    private AdminUserService adminUserService;

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private UserRepository userRepository;

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
                .build();

        Page<User> mockUserPage = new PageImpl<>(List.of(user1), pageable, 1);

        given(userRepository.findBySearchConditions(email, username, pageable)).willReturn(mockUserPage);

        // when
        Page<UserAdminResponse> result = adminUserService.getAllUsers(email, username, pageable);

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
        User user1 = User.builder().email("test1@test.com").username("user1").password("p").build();
        User user2 = User.builder().email("test2@test.com").username("user2").password("p").build();

        Page<User> mockUserPage = new PageImpl<>(List.of(user1, user2), pageable, 2);

        // 검색 조건이 null로 넘어오는 경우를 테스트
        given(userRepository.findBySearchConditions(null, null, pageable)).willReturn(mockUserPage);

        // when
        Page<UserAdminResponse> result = adminUserService.getAllUsers(null, null, pageable);

        // then
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).hasSize(2);
    }

    @Test
    @DisplayName("사용자 상세 조회 성공")
    void getUserDetails_Success() {
        // given
        Long userId = 1L;
        User mockUser = User.builder()
                .username("testuser")
                .email("test@test.com")
                .password("password")
                .build();

        given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));

        // when
        UserAdminResponse response = adminUserService.getUserDetails(userId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.email()).isEqualTo("test@test.com");
        assertThat(response.username()).isEqualTo("testuser");
        assertThat(response.point()).isEqualTo(new BigDecimal("1000"));
    }

    @Test
    @DisplayName("사용자 상세 조회 실패 - 존재하지 않는 사용자")
    void getUserDetails_Fail_UserNotFound() {
        // given
        Long userId = 999L;
        given(userRepository.findById(userId)).willReturn(Optional.empty());

        // when & then
        assertThrows(GlobalException.class, () -> {
            adminUserService.getUserDetails(userId);
        });
    }

    @Test
    @DisplayName("사용자 포인트 지급 성공")
    void adjustUserPoints_Success() {
        // given
        Long userId = 1L;
        BigDecimal amountToAdd = new BigDecimal("5000");
        PointAdjustRequest request = new PointAdjustRequest(amountToAdd, "베팅 승리 보상");

        // User 엔티티 생성 시 Builder는 point를 1000으로 초기화(초기 지급 포인트)
        User mockUser = User.builder()
                .email("test@test.com")
                .username("testuser")
                .password("password")
                .build();

        given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));

        // when
        PointAdjustResponse response = adminUserService.adjustUserPoints(userId, request);

        // then
        assertThat(response.userId()).isEqualTo(mockUser.getId());
        assertThat(response.adjustedAmount()).isEqualTo(amountToAdd);
        assertThat(response.finalBalance()).isEqualTo(new BigDecimal("6000"));
        assertThat(mockUser.getPoint()).isEqualTo(new BigDecimal("6000"));
    }

    @Test
    @DisplayName("사용자 포인트 지급 실패 - 존재하지 않는 사용자")
    void adjustUserPoints_Fail_UserNotFound() {
        // given
        Long userId = 999L;
        PointAdjustRequest request = new PointAdjustRequest(new BigDecimal("5000"), "이벤트 보상");
        given(userRepository.findById(userId)).willReturn(Optional.empty());

        // when & then
        assertThrows(GlobalException.class, () -> {
            adminUserService.adjustUserPoints(userId, request);
        });
    }
}
