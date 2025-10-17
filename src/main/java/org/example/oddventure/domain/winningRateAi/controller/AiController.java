package org.example.oddventure.domain.winningRateAi.controller;

import lombok.RequiredArgsConstructor;
import org.example.oddventure.domain.winningRateAi.service.AiService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/ai")
public class AiController {

    private final AiService aiService;

    @GetMapping("/winningrate")
    String generation(@RequestBody String userInput){
        return aiService.generateAbnormalBehaviorReport(userInput);
    }
}