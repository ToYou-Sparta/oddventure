package org.example.oddventure.domain.winningRateAi.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ai")
public class AiController {

    private final ChatClient chatClient;

    public AiController(ChatClient.Builder chatClient) {
        this.chatClient = chatClient.build();
    }

    @GetMapping("/winningrate")
    String generation(@RequestBody String userInput){
        return this.chatClient.prompt()
                .system("한국어로 대답하고 해당 팀의 전적과 계산한 승률에 대한 이유를 나열해.")
                .user(userInput)
                .call()
                .content();
    }
}