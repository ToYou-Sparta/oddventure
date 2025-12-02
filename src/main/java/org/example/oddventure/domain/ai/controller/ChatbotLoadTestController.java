package org.example.oddventure.domain.ai.controller;

import lombok.RequiredArgsConstructor;
import org.example.oddventure.common.dto.response.ApiResponse;
import org.example.oddventure.domain.ai.dto.UserMessage;
import org.example.oddventure.domain.ai.service.LoadTestChatbotService;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Profile("local")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/chat")
public class ChatbotLoadTestController {

    private final LoadTestChatbotService loadTestChatbotService;

    @PostMapping("/loadtest")
    public ResponseEntity<ApiResponse<String>> loadTest(
            @RequestBody UserMessage userMessage
    ) {
        String reply = loadTestChatbotService.reply(userMessage.message());
        return ApiResponse.success(reply, "부하 테스트용 응답입니다.");
    }
}
