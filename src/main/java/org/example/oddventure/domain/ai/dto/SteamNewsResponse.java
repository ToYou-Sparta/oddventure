package org.example.oddventure.domain.ai.dto;

import java.util.List;

public record SteamNewsResponse(Appnews appnews) {

    public record Appnews(List<Cs2NewsItem> newsitems) {
    }
}
