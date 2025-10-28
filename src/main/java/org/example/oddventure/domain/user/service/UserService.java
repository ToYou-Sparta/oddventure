package org.example.oddventure.domain.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.oddventure.domain.admin.dto.request.PointAdjustRequest;
import org.example.oddventure.domain.admin.dto.response.PointAdjustResponse;
import org.example.oddventure.domain.admin.exception.AdminErrorCode;
import org.example.oddventure.domain.admin.exception.AdminException;
import org.example.oddventure.domain.user.dto.request.PasswordUpdateRequest;
import org.example.oddventure.domain.user.dto.request.ProfileUpdateRequest;
import org.example.oddventure.domain.user.dto.response.UserProfileResponse;
import org.example.oddventure.domain.user.entity.User;
import org.example.oddventure.domain.user.exception.UserErrorCode;
import org.example.oddventure.domain.user.exception.UserException;
import org.example.oddventure.domain.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

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

    // 포인트 지급
    @Transactional
    public PointAdjustResponse adjustUserPoints(Long userId, PointAdjustRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AdminException(AdminErrorCode.USER_NOT_FOUND));

        user.plusPoint(request.amount());

        log.info("[ADMIN_POINT_ADJUSTMENT] userId={}, amount={}, reason='{}', finalBalance={}",
                userId, request.amount(), request.reason(), user.getPoint());

        return new PointAdjustResponse(
                user.getId(),
                user.getUsername(),
                request.amount(),
                user.getPoint()
        );
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.INVALID_USER_ID));
    }
}