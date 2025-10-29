package org.example.oddventure.domain.grid.dto.response;

import org.example.oddventure.domain.grid.dto.response.field.AllSeries;

public record AllSeriesResponse(
        Data data
) {
    public record Data(
            AllSeries allSeries
    ) {
    }
}
