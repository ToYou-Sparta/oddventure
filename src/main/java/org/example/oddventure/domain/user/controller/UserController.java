package org.example.oddventure.domain.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.oddventure.common.dto.response.ApiResponse;
import org.example.oddventure.domain.auth.dto.AuthUser;
import org.example.oddventure.domain.user.dto.request.PasswordUpdateRequest;
import org.example.oddventure.domain.user.dto.request.ProfileUpdateRequest;
import org.example.oddventure.domain.user.dto.response.UserProfileResponse;
import org.example.oddventure.domain.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getMyProfile(
            @AuthenticationPrincipal AuthUser authUser
    ) {
        UserProfileResponse response = userService.getUserProfile(authUser.id());
        return ApiResponse.success(response, "프로필 조회에 성공했습니다.");
    }

    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateMyProfile(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody ProfileUpdateRequest request
    ) {
        UserProfileResponse response = userService.updateUserProfile(authUser.id(), request);
        return ApiResponse.success(response, "프로필 수정에 성공했습니다.");
    }

    @PatchMapping("/password")
    public ResponseEntity<ApiResponse<Void>> updatePassword(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody PasswordUpdateRequest request
    ) {
        userService.updatePassword(authUser.id(), request);
        return ApiResponse.success(null, "비밀번호 변경에 성공했습니다.");
    }
}