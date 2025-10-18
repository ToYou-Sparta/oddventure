package org.example.oddventure.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.example.oddventure.common.exception.GlobalException;
import org.example.oddventure.domain.user.dto.request.PasswordUpdateRequest;
import org.example.oddventure.domain.user.dto.request.ProfileUpdateRequest;
import org.example.oddventure.domain.user.dto.response.UserProfileResponse;
import org.example.oddventure.domain.user.entity.User;
import org.example.oddventure.domain.user.exception.UserErrorCode;
import org.example.oddventure.domain.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile(Long userId)
    {
        User user = findUserById(userId);
        return UserProfileResponse.from(user);
    }

    public UserProfileResponse updateUserProfile(Long userId, ProfileUpdateRequest request)
    {
        User user = findUserById(userId);

        if (!user.getEmail().equals(request.email())) {
            if (userRepository.existsByEmail(request.email())) {
                throw new GlobalException(UserErrorCode.ALREADY_EXIST_EMAIL);
            }
        }

        user.updateProfile(request.username(), request.email());
        return UserProfileResponse.from(user);
    }

    public void updatePassword(Long userId, PasswordUpdateRequest request)
    {
        User user = findUserById(userId);

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new GlobalException(UserErrorCode.USR_PASSWORD_INCORRECT);
        }

        String newEncodedPassword = passwordEncoder.encode(request.newPassword());
        user.updatePassword(newEncodedPassword);
    }

    private User findUserById(Long userId)
    {
        return userRepository.findById(userId)
                .orElseThrow(() -> new GlobalException(UserErrorCode.USR_INVALID_USER_ID));
    }
}
