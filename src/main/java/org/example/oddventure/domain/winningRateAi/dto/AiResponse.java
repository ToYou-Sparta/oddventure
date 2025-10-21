package org.example.oddventure.domain.winningRateAi.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AiResponse(
        boolean result,
        boolean hasTeamName,
        List<String> teamName,
        List<Long> winningCount,
        List<Long> losingCount,
        List<Long> winningRate,
        String content
) {}

//변수가 하나라 여러 팀의 승률을 반환하지 못하는 문제 발생
//리스트로 반환
