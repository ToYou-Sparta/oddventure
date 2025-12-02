package org.example.oddventure.domain.ai.controller;

import lombok.RequiredArgsConstructor;
import org.example.oddventure.common.dto.response.ApiResponse;
import org.example.oddventure.domain.ai.agent.AgentExecutor;
import org.example.oddventure.domain.ai.agent.AgentRunnerService;
import org.example.oddventure.domain.ai.dto.UserMessage;
import org.example.oddventure.domain.auth.dto.AuthUser;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/chat")
public class ChatbotController {

    private final AgentRunnerService agentRunnerService;
    private final SimpMessagingTemplate messagingTemplate;

    @PostMapping
    public ResponseEntity<ApiResponse<AgentExecutor.State>> reply(
            @AuthenticationPrincipal AuthUser user,
            @RequestBody UserMessage userMessage
    ) throws Exception {
        AgentExecutor.State reply = agentRunnerService.execute(user.id(), userMessage.message()).orElse(null);
        messagingTemplate.convertAndSend("/topic/chat/" + user.id(), reply);

        return ApiResponse.success(reply, "AI 응답 테스트가 정상적으로 완료되었습니다.");
    }
}
