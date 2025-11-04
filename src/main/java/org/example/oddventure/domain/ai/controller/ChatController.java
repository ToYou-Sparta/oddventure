package org.example.oddventure.domain.ai.controller;

import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.example.oddventure.common.dto.response.ApiResponse;
import org.example.oddventure.domain.ai.service.ChatbotService;
import org.example.oddventure.domain.auth.dto.AuthUser;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/chat")
@Profile("local")
public class ChatController {

    private final ChatbotService chatbotService;

    // 로컬 테스트용
    @PostMapping
    public ResponseEntity<ApiResponse<String>> reply(
            @AuthenticationPrincipal AuthUser user,
            @RequestBody UserMessage userMessage
    ) {
        String reply = chatbotService.reply(user.id(), userMessage.message());
        return ApiResponse.success(reply, "AI 응답 테스트가 정상적으로 완료되었습니다.");
    }

    public record UserMessage(
            @NotBlank(message = "메시지를 입력해주세요!")
            String message
    ) {
    }
}
