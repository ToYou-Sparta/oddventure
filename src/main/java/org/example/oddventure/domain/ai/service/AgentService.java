package org.example.oddventure.domain.ai.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * Service class to interact with the AI-powered chat client for processing user inputs and generating responses.
 */
@Service
@Slf4j
public class AgentService {
    private final ChatClient chatClient;

    /**
     * Constructor for initializing the AgentService with a configured ChatClient.
     *
     * @param chatClientBuilder Builder for creating a ChatClient with predefined configurations.
     */
    public AgentService(ChatClient.Builder chatClientBuilder, AiService aiService) {
        this.chatClient = chatClientBuilder
                .defaultSystem("너는 e스포츠 데이터 기반 AI 챗봇이다. 사용자와의 맥락을 기억한다.")
                .defaultTools(aiService)
                .build();
    }

    /**
     * Executes a user query by passing it to the chat client and returning the response content.
     *
     * @param input    The user input or query to process.
     * @param messages A list of {@link ToolResponseMessage} providing context or tools for the AI to use.
     * @return The response content as a string.
     */
    public String execute(String input, List<ToolResponseMessage> messages) {
        try {
            // Validate input
            if (input == null || input.isBlank()) {
                throw new IllegalArgumentException("쿼리는 비어있을 수 없습니다.");
            }

            // Validate messages
            if (messages == null) {
                throw new IllegalArgumentException("메세지는 비어있을 수 없습니다.");
            }

            log.info("Executing query: {} with {} tool messages.", input, messages.size());

            return chatClient
                    .prompt()
                    .user(input)
                    .messages(messages.toArray(ToolResponseMessage[]::new))
                    .call()
                    .content();
        } catch (Exception e) {
            log.error("쿼리 실패 '{}': {}", input, e.getMessage(), e);
            throw new RuntimeException("쿼리 실패. 다음에 다시 시도하세요.", e);
        }
    }
}
