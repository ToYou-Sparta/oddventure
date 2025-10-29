package org.example.oddventure.domain.grid.dto.response.field;

public record PageInfo(
        boolean hasPreviousPage,
        boolean hasNextPage,
        String startCursor,
        String endCursor
) {
}
