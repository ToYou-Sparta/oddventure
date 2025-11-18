package org.example.oddventure.domain.admin.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.oddventure.common.dto.response.ApiResponse;
import org.example.oddventure.domain.admin.dto.request.MatchUpdateRequest;
import org.example.oddventure.domain.admin.dto.response.MatchCreateAdminResponse;
import org.example.oddventure.domain.admin.dto.response.MatchUpdateAdminResponse;
import org.example.oddventure.domain.admin.service.AdminMatchService;
import org.example.oddventure.domain.match.scheduler.MatchEsSyncScheduler;
import org.example.oddventure.domain.match.service.MatchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/matches")
public class AdminMatchController {

    private final AdminMatchService adminMatchService;
    private final MatchService matchService;
    private final MatchEsSyncScheduler matchEsSyncScheduler;

    // 매치 생성
    @PostMapping
    public ResponseEntity<ApiResponse<MatchCreateAdminResponse>> createMatch() {
        MatchCreateAdminResponse response = adminMatchService.createMatch();
        return ApiResponse.created(response, "매치가 생성되었습니다.");
    }

    // 매치 상태 수정
    @PatchMapping("/{matchId}")
    public ResponseEntity<ApiResponse<MatchUpdateAdminResponse>> updateMatchStatus(
            @PathVariable Long matchId,
            @Valid @RequestBody MatchUpdateRequest request
    ) {
        MatchUpdateAdminResponse response = matchService.updateMatch(matchId, request);
        return ApiResponse.success(response, "매치 정보가 수정되었습니다.");
    }

    // 매치 결과 연동
    @PatchMapping("/fetch/{fetchId}")
    public ResponseEntity<ApiResponse<Void>> createMatchResult(@PathVariable Long fetchId) {
        adminMatchService.createMatchResult(fetchId);
        return ApiResponse.success("매치 결과가 연동되었습니다.");
    }

    // 데이터 수동 동기화
    @PostMapping("/sync-elasticsearch")
    public ResponseEntity<ApiResponse<Void>> syncElasticsearch() {
        matchEsSyncScheduler.syncAllMatchesToElasticsearch();
        return ApiResponse.success("Elasticsearch 동기화가 시작되었습니다.");
    }
}
