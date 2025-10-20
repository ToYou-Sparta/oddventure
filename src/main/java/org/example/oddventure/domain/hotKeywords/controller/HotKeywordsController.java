package org.example.oddventure.domain.hotKeywords.controller;

import lombok.RequiredArgsConstructor;
import org.example.oddventure.common.dto.response.ApiResponse;
import org.example.oddventure.domain.hotKeywords.dto.HotKeywordsResponse;
import org.example.oddventure.domain.hotKeywords.service.HotKeywordsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class HotKeywordsController {
    private final HotKeywordsService hotKeywordsService;

    @GetMapping("/api/v1/hotkeyword")
    public ResponseEntity<ApiResponse<HotKeywordsResponse>> getHotKeywords() {
        return ApiResponse.success(hotKeywordsService.getHotKeywords(), "인기 검색어 top5를 조회했습니다.");
    }
}
