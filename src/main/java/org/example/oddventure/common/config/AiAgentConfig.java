package org.example.oddventure.common.config;

import org.example.oddventure.domain.ai.MatchConfigProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(MatchConfigProperties.class)
public class AiAgentConfig {
}
