package org.example.oddventure.domain.hotKeywords.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.Set;

@Getter
public class HotKeywordsResponse {
    private final Set<Object> hotKeywords;

    @Builder
    public HotKeywordsResponse(Set<Object> top5) {
        this.hotKeywords = top5;
    }

    public static HotKeywordsResponse of(Set<Object> top5) {
        return HotKeywordsResponse.builder()
                .top5(top5)
                .build();
    }
}
