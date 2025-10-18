package org.example.oddventure.user;

import org.example.oddventure.common.exception.GlobalException;
import org.example.oddventure.domain.user.dto.request.PasswordUpdateRequest;
import org.example.oddventure.domain.user.dto.request.ProfileUpdateRequest;
import org.example.oddventure.domain.user.dto.response.UserProfileResponse;
import org.example.oddventure.domain.user.entity.User;
import org.example.oddventure.domain.user.enums.UserRole;
import org.example.oddventure.domain.user.exception.UserErrorCode;
import org.example.oddventure.domain.user.repository.UserRepository;
import org.example.oddventure.domain.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import org.springframework.security.crypto.password.PasswordEncoder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

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
    void getUserProfile_Success() {
        // given
        Long userId = 1L;
        User mockUser = User.builder()
                .email("test@test.com")
                .username("testuser")
                .password("password")
                .userRole(UserRole.ROLE_USER)
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
    void updateUserProfile_Success_EmailChanged() {
        // given
        Long userId = 1L;
        String newUsername = "updatedUser";
        String newEmail = "new@email.com";
        ProfileUpdateRequest request = new ProfileUpdateRequest(newUsername, newEmail);

        User mockUser = User.builder()
                .email("original@email.com")
                .username("originalUser")
                .password("password")
                .userRole(UserRole.ROLE_USER)
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
    void updateUserProfile_Fail_EmailAlreadyExists() {
        // given
        Long userId = 1L;
        String newEmail = "already.exists@email.com";
        ProfileUpdateRequest request = new ProfileUpdateRequest("anyUser", newEmail);
        User mockUser = User.builder().email("original@email.com").build();

        given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));
        given(userRepository.existsByEmail(newEmail)).willReturn(true);

        // when & then
        GlobalException exception = assertThrows(GlobalException.class, () -> {
            userService.updateUserProfile(userId, request);
        });
        assertThat(exception.getErrorCode()).isEqualTo(UserErrorCode.ALREADY_EXIST_EMAIL);
    }

    @Test
    @DisplayName("프로필 수정 성공 - 이메일 변경 없음")
    void updateUserProfile_Success_EmailUnchanged() {
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
    void updatePassword_Success()
    {
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
    void updatePassword_Fail_PasswordIncorrect()
    {
        // given
        Long userId = 1L;
        PasswordUpdateRequest request = new PasswordUpdateRequest("wrongPassword!", "newPassword123!");
        User mockUser = User.builder().password("encodedCurrentPassword").build();

        given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));
        given(passwordEncoder.matches("wrongPassword!", "encodedCurrentPassword")).willReturn(false);

        // when & then
        assertThrows(GlobalException.class, () -> {
            userService.updatePassword(userId, request);
        });
    }
}