package org.example.oddventure.domain.match.dto;

import lombok.Builder;
import org.example.oddventure.domain.match.entity.Match;

@Builder
public record MatchCreateDto(
        Long fetchId
) {
    public static MatchCreateDto from(Match match) {
        return MatchCreateDto.builder()
                .fetchId(match.getFetchId())
                .build();
    }
}
