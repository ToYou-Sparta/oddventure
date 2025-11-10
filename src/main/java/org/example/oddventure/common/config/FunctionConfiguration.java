package org.example.oddventure.common.config;

import lombok.RequiredArgsConstructor;
import org.example.oddventure.domain.ai.service.AiService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class FunctionConfiguration {

    private final AiService aiService;

    @Bean
    public AiService bettingFunction() {
        return aiService;
    }

}