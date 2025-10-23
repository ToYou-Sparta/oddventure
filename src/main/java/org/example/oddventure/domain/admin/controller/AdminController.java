package org.example.oddventure.domain.admin.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.oddventure.common.dto.response.ApiPageResponse;
import org.example.oddventure.common.dto.response.ApiResponse;
import org.example.oddventure.domain.admin.dto.request.MatchCreateRequest;
import org.example.oddventure.domain.admin.dto.request.MatchUpdateRequest;
import org.example.oddventure.domain.admin.dto.request.PointAdjustRequest;
import org.example.oddventure.domain.admin.dto.response.MatchAdminResponse;
import org.example.oddventure.domain.admin.dto.response.PointAdjustResponse;
import org.example.oddventure.domain.admin.dto.response.UserAdminResponse;
import org.example.oddventure.domain.admin.service.AdminService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final AdminService adminService;

    // 매치 생성
    @PostMapping("/matches")
    public ResponseEntity<ApiResponse<MatchAdminResponse>> createMatch(
            @Valid @RequestBody MatchCreateRequest request
    ) {
        MatchAdminResponse response = adminService.createMatch(request);
        return ApiResponse.created(response, "매치가 생성되었습니다.");
    }

    // 매치 상태 수정
    @PatchMapping("/matches/{matchId}")
    public ResponseEntity<ApiResponse<MatchAdminResponse>> updateMatchStatus(
            @PathVariable Long matchId,
            @Valid @RequestBody MatchUpdateRequest request
    ) {
        MatchAdminResponse response = adminService.updateMatch(matchId, request);
        return ApiResponse.success(response, "매치 정보가 수정되었습니다.");
    }

    // 전체 사용자 목록 조회
    @GetMapping("/users")
    public ResponseEntity<ApiPageResponse<UserAdminResponse>> getAllUsers(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String username,
            Pageable pageable
    ) {
        Page<UserAdminResponse> users = adminService.getAllUsers(email, username, pageable);
        return ApiPageResponse.success(users, "사용자 목록 조회에 성공했습니다.");
    }

    // 사용자 상세 조회
    @GetMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<UserAdminResponse>> getUserDetails(@PathVariable Long userId) {
        UserAdminResponse response = adminService.getUserDetails(userId);
        return ApiResponse.success(response, "사용자 상세 정보 조회에 성공했습니다.");
    }

    // 포인트 지급
    @PostMapping("/users/{userId}/points")
    public ResponseEntity<ApiResponse<PointAdjustResponse>> adjustUserPoints(
            @PathVariable Long userId,
            @Valid @RequestBody PointAdjustRequest request
    ) {
        PointAdjustResponse response = adminService.adjustUserPoints(userId, request);
        return ApiResponse.success(response, "포인트 지급에 성공했습니다.");
    }
}