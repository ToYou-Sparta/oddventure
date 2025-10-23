package org.example.oddventure.domain.ai.controller;

import lombok.RequiredArgsConstructor;
import org.example.oddventure.common.dto.response.ApiResponse;
import org.example.oddventure.domain.ai.dto.AiRequest;
import org.example.oddventure.domain.ai.dto.AiResponse;
import org.example.oddventure.domain.ai.service.AiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/ai")
public class AiController {

    private final AiService aiService;

    @PostMapping("/winningrate")
    ResponseEntity<ApiResponse<AiResponse>> generation(@RequestBody String content) {
        AiRequest request = new AiRequest(content);
        return ApiResponse.success(aiService.calculateWinningRateWithAi(request), "ai 기반 승률 예측 결과를 반환한다.");
    }
}