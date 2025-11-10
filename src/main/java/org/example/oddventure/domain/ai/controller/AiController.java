package org.example.oddventure.domain.ai.controller;

import lombok.RequiredArgsConstructor;
import org.example.oddventure.domain.ai.AgentRunnerService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/ai")
public class AiController {

    private final AgentRunnerService agentRunnerService;

    @PostMapping("/winningrate")
    public Object generation(@RequestBody String request) throws Exception {
        return agentRunnerService.execute(request);
    }
}