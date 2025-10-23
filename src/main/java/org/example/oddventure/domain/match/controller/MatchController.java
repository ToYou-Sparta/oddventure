package org.example.oddventure.domain.match.controller;

import lombok.RequiredArgsConstructor;
import org.example.oddventure.common.dto.response.ApiPageResponse;
import org.example.oddventure.common.dto.response.ApiResponse;
import org.example.oddventure.domain.match.dto.request.MatchSearchCondition;
import org.example.oddventure.domain.match.dto.response.MatchResponse;
import org.example.oddventure.domain.match.service.MatchService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
@RequestMapping("/api/v1/matches")
public class MatchController {

    private final MatchService matchService;

    @GetMapping
    public ResponseEntity<ApiPageResponse<MatchResponse>> getMatches(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("startTime").ascending());
        Page<MatchResponse> matches = matchService.getMatches(pageable);
        return ApiPageResponse.success(matches, "매치 목록 조회에 성공했습니다.");
    }

    @GetMapping("/{matchId}")
    public ResponseEntity<ApiResponse<MatchResponse>> getMatch(
            @PathVariable Long matchId
    ) {
        MatchResponse match = matchService.getMatch(matchId);
        return ApiResponse.success(match, "매치 상세 조회에 성공했습니다.");
    }

    @PostMapping("/search")
    public ResponseEntity<ApiPageResponse<MatchResponse>> searchMatches(
            @RequestBody MatchSearchCondition condition,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<MatchResponse> matches = matchService.searchMatches(condition, pageable);
        return ApiPageResponse.success(matches, "검색 조건에 맞는 매치 목록을 조회했습니다.");
    }
}