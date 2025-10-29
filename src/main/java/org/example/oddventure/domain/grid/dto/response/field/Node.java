package org.example.oddventure.domain.grid.dto.response.field;

import java.util.List;

public record Node(
        String id,
        Title title,
        Tournament tournament,
        String startTimeScheduled,
        Format format,
        List<Team> teams
) {
    public record Title(String nameShortened) {
    }

    public record Tournament(String nameShortened) {
    }

    public record Format(String nameShortened) {
    }

    public record Team(BaseInfo baseInfo) {
        public record BaseInfo(String name) {
        }
    }
}