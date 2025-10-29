package org.example.oddventure.domain.admin.dto.response;

import java.util.List;
import lombok.Builder;

@Builder
public record MatchCreateAdminResponse(
        int totalMatches,
        List<Long> fetchIds
) {
    public static MatchCreateAdminResponse of(List<Long> fetchIds) {
        int cnt = fetchIds.size();
        return MatchCreateAdminResponse.builder()
                .totalMatches(cnt)
                .fetchIds(fetchIds)
                .build();
    }
}