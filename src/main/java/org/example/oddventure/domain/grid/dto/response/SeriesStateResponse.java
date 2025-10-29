package org.example.oddventure.domain.grid.dto.response;

import org.example.oddventure.domain.grid.dto.response.field.SeriesState;

public record SeriesStateResponse(
        Data data
) {
    public record Data(
            SeriesState seriesState
    ) {
    }
}
