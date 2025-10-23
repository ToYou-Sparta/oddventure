package org.example.oddventure.domain.auth.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.example.oddventure.common.aop.ValidUser;
import org.example.oddventure.common.dto.response.ApiResponse;
import org.example.oddventure.domain.auth.dto.AuthUser;
import org.example.oddventure.domain.auth.dto.request.LoginRequest;
import org.example.oddventure.domain.auth.dto.request.SignupRequest;
import org.example.oddventure.domain.auth.dto.request.WithdrawRequest;
import org.example.oddventure.domain.auth.dto.response.AccessTokenResponse;
import org.example.oddventure.domain.auth.dto.response.LoginResponse;
import org.example.oddventure.domain.auth.dto.response.SignupResponse;
import org.example.oddventure.domain.auth.service.AuthService;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignupResponse>> signup(
            @Valid @RequestBody SignupRequest request
    ) {
        SignupResponse response = authService.signup(request);
        return ApiResponse.created(response, "회원가입이 완료되었습니다.");
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse httpResponse
    ) {
        LoginResponse response = authService.login(request);

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", response.refreshToken())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(Duration.ofDays(7))
                .sameSite("Strict")
                .build();

        httpResponse.addHeader("Set-Cookie", refreshCookie.toString());

        return ApiResponse.success(response, "로그인 되었습니다.");
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(
            @AuthenticationPrincipal AuthUser user,
            HttpServletResponse httpResponse
    ) {
        authService.logout(user.id());

        ResponseCookie deleteCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .build();

        httpResponse.addHeader("Set-Cookie", deleteCookie.toString());

        return ApiResponse.success(null, "로그아웃 되었습니다.");
    }

    @ValidUser
    @DeleteMapping("/withdraw")
    public ResponseEntity<ApiResponse<String>> withdraw(
            @AuthenticationPrincipal AuthUser user,
            @Valid @RequestBody WithdrawRequest request
    ) {
        authService.withdraw(user.id(), request);
        return ApiResponse.success(null, "회원탈퇴 되었습니다.");
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AccessTokenResponse>> refresh(
            @CookieValue(value = "refreshToken", required = false) String refreshToken
    ) {
        AccessTokenResponse newAccessToken = authService.refresh(refreshToken);
        return ApiResponse.success(newAccessToken, "토큰이 재발급되었습니다.");
    }
}
