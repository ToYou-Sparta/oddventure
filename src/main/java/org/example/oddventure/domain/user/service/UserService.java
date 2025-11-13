package org.example.oddventure.domain.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.oddventure.common.exception.CommonErrorCode;
import org.example.oddventure.domain.admin.dto.request.PointAdjustRequest;
import org.example.oddventure.domain.admin.dto.response.PointAdjustResponse;
import org.example.oddventure.domain.bet.exception.BetException;
import org.example.oddventure.domain.user.dto.request.PasswordUpdateRequest;
import org.example.oddventure.domain.user.dto.request.ProfileUpdateRequest;
import org.example.oddventure.domain.user.dto.response.UserProfileResponse;
import org.example.oddventure.domain.user.entity.User;
import org.example.oddventure.domain.user.exception.UserErrorCode;
import org.example.oddventure.domain.user.exception.UserException;
import org.example.oddventure.domain.user.repository.UserRepository;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import java.util.concurrent.TimeUnit;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RedissonClient redissonClient;
    private final UserPointTransactionService userPointTransactionService;
    private static final String POINT_LOCK_PREFIX = "LOCK:USER_POINT:";

    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile(Long userId) {
        User user = findUserById(userId);
        return UserProfileResponse.from(user);
    }

    @Transactional
    public UserProfileResponse updateUserProfile(Long userId, ProfileUpdateRequest request) {
        User user = findUserById(userId);

        if (request.email() != null & !user.getEmail().equals(request.email())) {
            if (userRepository.existsByEmail(request.email())) {
                throw new UserException(UserErrorCode.ALREADY_EXIST_EMAIL);
            }
        }

        user.updateProfile(request.username(), request.email());

        return UserProfileResponse.from(user);
    }

    @Transactional
    public void updatePassword(Long userId, PasswordUpdateRequest request) {
        User user = findUserById(userId);

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new UserException(UserErrorCode.PASSWORD_INCORRECT);
        }

        String newEncodedPassword = passwordEncoder.encode(request.newPassword());

        user.updatePassword(newEncodedPassword);
    }

    public PointAdjustResponse adjustUserPoints(Long userId, PointAdjustRequest request) {
        String lockKey = POINT_LOCK_PREFIX + userId;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            boolean isLocked = lock.tryLock(10, 5, TimeUnit.SECONDS);

            if (!isLocked) {
                log.warn("포인트 지급 락 획득 실패. userId: {}", userId);
                throw new BetException(org.example.oddventure.domain.bet.exception.BetErrorCode.BET_LOCK_FAILED);
            }

            User user = userPointTransactionService.increaseUserPoint(userId, request);

            log.info("[ADMIN_POINT_ADJUSTMENT] userId={}, amount={}, reason='{}', finalBalance={}",
                    userId, request.amount(), request.reason(), user.getPoint());

            return new PointAdjustResponse(
                    user.getId(),
                    user.getUsername(),
                    request.amount(),
                    user.getPoint()
            );

        } catch (InterruptedException e) {
            log.error("포인트 지급 락 대기 중 인터럽트 발생", e);
            Thread.currentThread().interrupt();
            throw new UserException(CommonErrorCode.INTERNAL_SERVER_ERROR);
        } finally {
            if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.INVALID_USER_ID));
    }
}