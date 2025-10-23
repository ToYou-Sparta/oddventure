package org.example.oddventure.domain.auth.service;

import static org.example.oddventure.domain.auth.jwt.JwtConstants.REFRESH_TOKEN_PREFIX;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.example.oddventure.domain.auth.dto.request.LoginRequest;
import org.example.oddventure.domain.auth.dto.request.SignupRequest;
import org.example.oddventure.domain.auth.dto.request.WithdrawRequest;
import org.example.oddventure.domain.auth.dto.response.AccessTokenResponse;
import org.example.oddventure.domain.auth.dto.response.LoginResponse;
import org.example.oddventure.domain.auth.dto.response.SignupResponse;
import org.example.oddventure.domain.auth.exception.AuthErrorCode;
import org.example.oddventure.domain.auth.exception.AuthException;
import org.example.oddventure.domain.auth.jwt.JwtConstants;
import org.example.oddventure.domain.auth.jwt.JwtUtil;
import org.example.oddventure.domain.user.entity.User;
import org.example.oddventure.domain.user.exception.UserErrorCode;
import org.example.oddventure.domain.user.exception.UserException;
import org.example.oddventure.domain.user.repository.UserRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, String> redisTemplate;

    @Transactional
    public SignupResponse signup(SignupRequest request) {
        if (userRepository.existsByEmail((request.email()))) {
            throw new UserException(UserErrorCode.ALREADY_EXIST_EMAIL);
        }

        String encodedPassword = passwordEncoder.encode(request.password());
        User user = User.builder()
                .username(request.username())
                .email(request.email())
                .password(encodedPassword)
                .build();
        User savedUser = userRepository.save(user);

        return SignupResponse.from(savedUser);
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new AuthException(AuthErrorCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new AuthException(AuthErrorCode.INVALID_CREDENTIALS);
        }

        String accessToken = jwtUtil.createAccessToken(user.getId(), user.getUserRole());
        String refreshToken = jwtUtil.createRefreshToken(user.getId());

        redisTemplate.opsForValue().set(
                REFRESH_TOKEN_PREFIX + user.getId(),
                refreshToken,
                Duration.ofMillis(JwtConstants.REFRESH_TOKEN_EXPIRATION)
        );

        return LoginResponse.of(accessToken, refreshToken);
    }

    @Transactional
    public void logout(Long userId) {
        redisTemplate.delete(REFRESH_TOKEN_PREFIX + userId);
    }

    @Transactional
    public void withdraw(Long userId, WithdrawRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new UserException(UserErrorCode.PASSWORD_INCORRECT);
        }

        if (user.isDeleted()) {
            throw new UserException(UserErrorCode.ALREADY_WITHDRAWN);
        }

        user.delete();
        redisTemplate.delete(REFRESH_TOKEN_PREFIX + userId);
    }

    public AccessTokenResponse refresh(String refreshToken) {
        if (refreshToken == null || !jwtUtil.validateToken(refreshToken)) {
            throw new AuthException(AuthErrorCode.TOKEN_INVALID);
        }

        Long userId = jwtUtil.extractUserId(refreshToken);
        String storedToken = redisTemplate.opsForValue().get(REFRESH_TOKEN_PREFIX + userId);

        if (storedToken == null || !storedToken.equals(refreshToken)) {
            throw new AuthException(AuthErrorCode.REFRESH_TOKEN_MISMATCH);
        }

        String newAccessToken = jwtUtil.createAccessToken(userId, jwtUtil.extractUserRole(refreshToken));

        return AccessTokenResponse.of(newAccessToken);
    }
}
