package org.example.oddventure.domain.match.dto.request;

import java.time.LocalDateTime;

public record MatchSearchCondition(
        String keyword,
        LocalDateTime fromDate,
        LocalDateTime toDate
) {
}
