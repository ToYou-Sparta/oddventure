package org.example.oddventure.domain.grid.dto.response.field;

import java.util.List;

public record AllSeries(
        int totalCount,
        PageInfo pageInfo,
        List<Edge> edges
) {
}
