package org.example.oddventure.domain.ai.controller;

import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.example.oddventure.common.dto.response.ApiResponse;
import org.example.oddventure.domain.ai.agent.AgentExecutor;
import org.example.oddventure.domain.ai.agent.AgentRunnerService;
import org.example.oddventure.domain.ai.pubsub.RedisPublisher;
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
public class ChatbotController {

    private final AgentRunnerService agentRunnerService;
    private final RedisPublisher redisPublisher;

    // 로컬 테스트용
    @PostMapping
    public ResponseEntity<ApiResponse<AgentExecutor.State>> reply(
            @AuthenticationPrincipal AuthUser user,
            @RequestBody UserMessage userMessage
    ) throws Exception {
        // 테스트용 Redis 직접 발행
        String channel = "chat:" + user.id() + ":input";
        redisPublisher.publish(channel, userMessage);

        AgentExecutor.State reply = agentRunnerService.execute(user.id(), userMessage.message()).orElse(null);
        return ApiResponse.success(reply, "AI 응답 테스트가 정상적으로 완료되었습니다.");
    }

    public record UserMessage(
            @NotBlank(message = "메시지를 입력해주세요!")
            String message
    ) {
    }
}
