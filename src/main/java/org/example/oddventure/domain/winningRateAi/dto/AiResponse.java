package org.example.oddventure.domain.winningRateAi.dto;

public record AiResponse(
        boolean result,
        boolean hasTeamName,
        String teamName,
        Long winningCount,
        Long losingCount,
        String winningRate,
        String content
) {}
