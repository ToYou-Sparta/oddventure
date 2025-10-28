package org.example.oddventure.domain.admin.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.oddventure.common.dto.response.ApiPageResponse;
import org.example.oddventure.common.dto.response.ApiResponse;
import org.example.oddventure.domain.admin.dto.request.PointAdjustRequest;
import org.example.oddventure.domain.admin.dto.response.PointAdjustResponse;
import org.example.oddventure.domain.admin.dto.response.UserAdminResponse;
import org.example.oddventure.domain.admin.service.AdminUserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/users")
public class AdminUserController {

    private final AdminUserService adminUserService;

    // 전체 사용자 목록 조회
    @GetMapping
    public ResponseEntity<ApiPageResponse<UserAdminResponse>> getAllUsers(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String username,
            Pageable pageable
    ) {
        Page<UserAdminResponse> users = adminUserService.getAllUsers(email, username, pageable);
        return ApiPageResponse.success(users, "사용자 목록 조회에 성공했습니다.");
    }

    // 사용자 상세 조회
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserAdminResponse>> getUserDetails(@PathVariable Long userId) {
        UserAdminResponse response = adminUserService.getUserDetails(userId);
        return ApiResponse.success(response, "사용자 상세 정보 조회에 성공했습니다.");
    }

    // 포인트 지급
    @PostMapping("/{userId}/points")
    public ResponseEntity<ApiResponse<PointAdjustResponse>> adjustUserPoints(
            @PathVariable Long userId,
            @Valid @RequestBody PointAdjustRequest request
    ) {
        PointAdjustResponse response = adminUserService.adjustUserPoints(userId, request);
        return ApiResponse.success(response, "포인트 지급에 성공했습니다.");
    }
}
