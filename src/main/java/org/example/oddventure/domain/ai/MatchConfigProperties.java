package org.example.oddventure.domain.ai;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(value = "grid")
public record MatchConfigProperties(String apiKey, String apiUrl) {

}