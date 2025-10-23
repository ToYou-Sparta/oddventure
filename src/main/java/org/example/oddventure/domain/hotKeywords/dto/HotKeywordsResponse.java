package org.example.oddventure.domain.hotKeywords.dto;

import java.util.Set;

public record HotKeywordsResponse(Set<Object> hotKeywords) {

    public static HotKeywordsResponse of(Set<Object> top5) {
        return new HotKeywordsResponse(top5);
    }
}