package org.example.oddventure.domain.winningRateAi.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AiResponse(
        boolean result,
        boolean hasTeamName,
        String teamName,
        Long winningCount,
        Long losingCount,
        String winningRate,
        String content
) {}
