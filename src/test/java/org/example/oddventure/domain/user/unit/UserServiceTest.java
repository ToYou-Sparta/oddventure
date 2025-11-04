package org.example.oddventure.domain.user.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.example.oddventure.common.exception.GlobalException;
import org.example.oddventure.domain.admin.dto.request.PointAdjustRequest;
import org.example.oddventure.domain.admin.dto.response.PointAdjustResponse;
import org.example.oddventure.domain.admin.exception.AdminErrorCode;
import org.example.oddventure.domain.bet.exception.BetErrorCode;
import org.example.oddventure.domain.bet.exception.BetException;
import org.example.oddventure.domain.user.dto.request.PasswordUpdateRequest;
import org.example.oddventure.domain.user.dto.request.ProfileUpdateRequest;
import org.example.oddventure.domain.user.dto.response.UserProfileResponse;
import org.example.oddventure.domain.user.entity.User;
import org.example.oddventure.domain.user.exception.UserErrorCode;
import org.example.oddventure.domain.user.exception.UserException;
import org.example.oddventure.domain.user.repository.UserRepository;
import org.example.oddventure.domain.user.service.UserService;
import org.example.oddventure.domain.user.service.UserPointTransactionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RedissonClient redissonClient;

    @Mock
    private UserPointTransactionService userPointTransactionService;

    @Mock
    private RLock lock;

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

    @Nested
    @DisplayName("사용자 포인트 지급")
    class adjustUserPoint {
        @Test
        @DisplayName("사용자 포인트 지급 성공 (분산 락)")
        void adjustUserPoints_Success() throws InterruptedException {
            // given
            Long userId = 1L;
            BigDecimal amountToAdd = new BigDecimal("5000");
            PointAdjustRequest request = new PointAdjustRequest(amountToAdd, "베팅 승리 보상");

            User mockUser = User.builder()
                    .email("test@test.com")
                    .username("testuser")
                    .password("password")
                    .build();
            // User 엔티티는 1000포인트로 시작
            mockUser.plusPoint(amountToAdd); // 6000 포인트가 된 유저 Mock

            // Redisson 락 Mocking
            given(redissonClient.getLock(any(String.class))).willReturn(lock);
            given(lock.tryLock(10, 5, TimeUnit.SECONDS)).willReturn(true); // 락 획득 성공

            // 트랜잭션 서비스 Mocking
            given(userPointTransactionService.increaseUserPoint(userId, request))
                    .willReturn(mockUser); // 6000 포인트 유저 반환

            // when
            PointAdjustResponse response = userService.adjustUserPoints(userId, request);

            // then
            assertThat(response.userId()).isEqualTo(mockUser.getId());
            assertThat(response.adjustedAmount()).isEqualTo(amountToAdd);
            assertThat(response.finalBalance()).isEqualTo(new BigDecimal("6000")); // 1000 + 5000
        }

        @Test
        @DisplayName("사용자 포인트 지급 실패 - 락 획득 실패")
        void adjustUserPoints_Fail_LockFailed() throws InterruptedException {
            // given
            Long userId = 1L;
            PointAdjustRequest request = new PointAdjustRequest(BigDecimal.TEN, "이벤트 보상");

            given(redissonClient.getLock(any(String.class))).willReturn(lock);
            given(lock.tryLock(10, 5, TimeUnit.SECONDS)).willReturn(false); // 락 획득 실패

            // when & then
            BetException exception = assertThrows(BetException.class, () -> {
                userService.adjustUserPoints(userId, request);
            });
            assertThat(exception.getErrorCode()).isEqualTo(BetErrorCode.BET_LOCK_FAILED);
        }

        @Test
        @DisplayName("사용자 포인트 지급 실패 - 트랜잭션 중 예외 발생")
        void adjustUserPoints_Fail_UserNotFoundInTransaction() throws InterruptedException {
            // given
            Long userId = 999L;
            PointAdjustRequest request = new PointAdjustRequest(BigDecimal.TEN, "이벤트 보상");

            given(redissonClient.getLock(any(String.class))).willReturn(lock);
            given(lock.tryLock(10, 5, TimeUnit.SECONDS)).willReturn(true); // 락 획득 성공

            // 트랜잭션 서비스에서 예외 발생
            given(userPointTransactionService.increaseUserPoint(userId, request))
                    .willThrow(new GlobalException(AdminErrorCode.USER_NOT_FOUND));

            // when & then
            assertThrows(GlobalException.class, () -> {
                userService.adjustUserPoints(userId, request);
            });
        }
    }

}