package org.example.oddventure.domain.user.controller;

import jakarta.validation.Valid;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.example.oddventure.common.dto.response.ApiResponse;
import org.example.oddventure.domain.user.dto.request.PasswordUpdateRequest;
import org.example.oddventure.domain.user.dto.request.ProfileUpdateRequest;
import org.example.oddventure.domain.user.dto.response.UserProfileResponse;
import org.example.oddventure.domain.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getMyProfile(
            Principal principal)
    {
        Long userId = Long.parseLong(principal.getName());
        UserProfileResponse response = userService.getUserProfile(userId);

        return ApiResponse.success(response, "프로필 조회에 성공했습니다.");
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateMyProfile(
            Principal principal,
            @Valid @RequestBody ProfileUpdateRequest request)
    {
        Long userId = Long.parseLong(principal.getName());
        UserProfileResponse response = userService.updateUserProfile(userId, request);

        return ApiResponse.success(response, "프로필 수정에 성공했습니다.");
    }

    @PutMapping("/password")
    public ResponseEntity<ApiResponse<Void>> updatePassword(
            Principal principal,
            @Valid @RequestBody PasswordUpdateRequest request)
    {
        Long userId = Long.parseLong(principal.getName());
        userService.updatePassword(userId, request);

        return ApiResponse.success(null, "비밀번호 변경에 성공했습니다.");
    }
}
