package org.example.oddventure.domain.auth.service;

import static org.example.oddventure.domain.auth.jwt.JwtConstants.REFRESH_TOKEN_PREFIX;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.example.oddventure.domain.auth.dto.request.LoginRequest;
import org.example.oddventure.domain.auth.dto.request.SignupRequest;
import org.example.oddventure.domain.auth.dto.request.WithdrawRequest;
import org.example.oddventure.domain.auth.dto.response.LoginResponse;
import org.example.oddventure.domain.auth.dto.response.SignupResponse;
import org.example.oddventure.domain.auth.exception.AuthErrorCode;
import org.example.oddventure.domain.auth.exception.AuthException;
import org.example.oddventure.domain.auth.jwt.JwtUtil;
import org.example.oddventure.domain.user.entity.User;
import org.example.oddventure.domain.user.enums.UserRole;
import org.example.oddventure.domain.user.exception.InvalidUserException;
import org.example.oddventure.domain.user.exception.UserErrorCode;
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

    /**
     * 회원가입
     *
     * @param request 회원가입 요청 정보
     * @return 회원가입 응답 정보
     */
    @Transactional
    public SignupResponse signup(SignupRequest request) {

        if (userRepository.existsByEmail((request.email()))) {
            throw new InvalidUserException(UserErrorCode.ALREADY_EXIST_EMAIL);
        }

        String encodedPassword = passwordEncoder.encode(request.password());
        User user = User.builder()
                .username(request.username())
                .email(request.email())
                .password(encodedPassword)
                .userRole(UserRole.ROLE_USER)
                .build();
        User savedUser = userRepository.save(user);

        return SignupResponse.from(savedUser);
    }

    /**
     * 로그인
     *
     * @param request 로그인 요청 정보
     * @return 로그인 응답 정보
     */
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new AuthException(AuthErrorCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new AuthException(AuthErrorCode.INVALID_CREDENTIALS);
        }

        String accessToken = jwtUtil.createAccessToken(user.getId(), user.getUserRole());
        String refreshToken = jwtUtil.createRefreshToken(user.getId());

        redisTemplate.opsForValue().set(REFRESH_TOKEN_PREFIX + user.getId(), refreshToken, Duration.ofDays(7));

        return LoginResponse.of(accessToken, refreshToken);
    }

    /**
     * 로그아웃
     *
     * @param userId 로그인한 사용자의 고유 ID
     */
    @Transactional(readOnly = true)
    public void logout(Long userId) {
        redisTemplate.delete(REFRESH_TOKEN_PREFIX + userId);
    }

    /**
     * 회원 탈퇴
     *
     * @param token   Access Token
     * @param request 회원 탈퇴 요청 정보 (비밀번호 확인)
     */
    public void withdraw(String token, WithdrawRequest request) {

    }
}
