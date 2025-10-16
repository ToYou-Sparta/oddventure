package org.example.oddventure.domain.admin.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.oddventure.common.dto.response.ApiResponse;
import org.example.oddventure.domain.admin.dto.request.MatchCreateRequest;
import org.example.oddventure.domain.admin.dto.request.MatchUpdateRequest;
import org.example.oddventure.domain.admin.dto.response.MatchAdminResponse;
import org.example.oddventure.domain.admin.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final AdminService adminService;

    // 매치 생성
    @PostMapping("/matches")
    public ResponseEntity<ApiResponse<MatchAdminResponse>> createMatch(
            @Valid @RequestBody MatchCreateRequest request) {
        MatchAdminResponse response = adminService.createMatch(request);
        return ApiResponse.created(response);
    }

    // 매치 상태 수정
    @PatchMapping("/matches/{matchId}")
    public ResponseEntity<ApiResponse<MatchAdminResponse>> updateMatchStatus(
            @PathVariable Long matchId,
            @Valid @RequestBody MatchUpdateRequest request) {
        MatchAdminResponse response = adminService.updateMatch(matchId, request);
        return ApiResponse.success(response);
    }
}