package org.example.oddventure.domain.user.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.util.Optional;
import org.example.oddventure.common.exception.GlobalException;
import org.example.oddventure.domain.admin.dto.request.PointAdjustRequest;
import org.example.oddventure.domain.admin.dto.response.PointAdjustResponse;
import org.example.oddventure.domain.user.dto.request.PasswordUpdateRequest;
import org.example.oddventure.domain.user.dto.request.ProfileUpdateRequest;
import org.example.oddventure.domain.user.dto.response.UserProfileResponse;
import org.example.oddventure.domain.user.entity.User;
import org.example.oddventure.domain.user.exception.UserErrorCode;
import org.example.oddventure.domain.user.exception.UserException;
import org.example.oddventure.domain.user.repository.UserRepository;
import org.example.oddventure.domain.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("사용자 프로필 조회 성공")
    void getUserProfile_success() {
        // given
        Long userId = 1L;
        User mockUser = User.builder()
                .email("test@test.com")
                .username("testuser")
                .password("password")
                .build();
        given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));

        // when
        UserProfileResponse response = userService.getUserProfile(userId);

        // then
        assertThat(response.email()).isEqualTo("test@test.com");
        assertThat(response.username()).isEqualTo("testuser");
    }

    @Test
    @DisplayName("프로필 수정 성공 - 이메일 변경")
    void updateUserProfile_success_emailChanged() {
        // given
        Long userId = 1L;
        String newUsername = "updatedUser";
        String newEmail = "new@email.com";
        ProfileUpdateRequest request = new ProfileUpdateRequest(newUsername, newEmail);

        User mockUser = User.builder()
                .email("original@email.com")
                .username("originalUser")
                .password("password")
                .build();

        given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));
        given(userRepository.existsByEmail(newEmail)).willReturn(false);

        // when
        UserProfileResponse response = userService.updateUserProfile(userId, request);

        // then
        assertThat(mockUser.getUsername()).isEqualTo(newUsername);
        assertThat(mockUser.getEmail()).isEqualTo(newEmail);
        assertThat(response.username()).isEqualTo(newUsername);
        assertThat(response.email()).isEqualTo(newEmail);
    }

    @Test
    @DisplayName("프로필 수정 실패 - 이메일 중복")
    void updateUserProfile_fail_emailAlreadyExists() {
        // given
        Long userId = 1L;
        String newEmail = "already.exists@email.com";
        ProfileUpdateRequest request = new ProfileUpdateRequest("anyUser", newEmail);
        User mockUser = User.builder().email("original@email.com").build();

        given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));
        given(userRepository.existsByEmail(newEmail)).willReturn(true);

        // when & then
        UserException exception = assertThrows(UserException.class, () -> {
            userService.updateUserProfile(userId, request);
        });
        assertThat(exception.getErrorCode()).isEqualTo(UserErrorCode.ALREADY_EXIST_EMAIL);
    }

    @Test
    @DisplayName("프로필 수정 성공 - 이메일 변경 없음")
    void updateUserProfile_success_emailUnchanged() {
        // given
        Long userId = 1L;
        String sameEmail = "original@email.com";
        ProfileUpdateRequest request = new ProfileUpdateRequest("updatedUser", sameEmail);
        User mockUser = User.builder().email(sameEmail).username("originalUser").build();

        given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));

        // when
        userService.updateUserProfile(userId, request);

        // then
        verify(userRepository, never()).existsByEmail(any());
        assertThat(mockUser.getUsername()).isEqualTo("updatedUser");
    }

    @Test
    @DisplayName("비밀번호 변경 성공")
    void updatePassword_success() {
        // given
        Long userId = 1L;
        PasswordUpdateRequest request = new PasswordUpdateRequest("currentPassword123!", "newPassword123!");
        User mockUser = User.builder().password("encodedCurrentPassword").build();

        given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));
        given(passwordEncoder.matches("currentPassword123!", "encodedCurrentPassword")).willReturn(true);

        // when
        userService.updatePassword(userId, request);

        // then
        verify(passwordEncoder).encode("newPassword123!");
    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 현재 비밀번호 불일치")
    void updatePassword_fail_passwordIncorrect() {
        // given
        Long userId = 1L;
        PasswordUpdateRequest request = new PasswordUpdateRequest("wrongPassword!", "newPassword123!");
        User mockUser = User.builder().password("encodedCurrentPassword").build();

        given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));
        given(passwordEncoder.matches("wrongPassword!", "encodedCurrentPassword")).willReturn(false);

        // when & then
        assertThrows(UserException.class, () -> {
            userService.updatePassword(userId, request);
        });
    }

    @Nested
    @DisplayName("사용자 포인트 지급")
    class adjustUserPoint {
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

            given(userRepository.findByIdForUpdate(userId)).willReturn(Optional.of(mockUser));

            // when
            PointAdjustResponse response = userService.adjustUserPoints(userId, request);

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
            given(userRepository.findByIdForUpdate(userId)).willReturn(Optional.empty());

            // when & then
            assertThrows(GlobalException.class, () -> {
                userService.adjustUserPoints(userId, request);
            });
        }
    }

}