package org.example.oddventure.domain.ai.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record WinRateResponse(
        boolean result,
        List<WinRateItem> items,
        String compareTarget,
        String content,
        String message
) {
    public static WinRateResponse ok(List<WinRateItem> items, String compareTarget, String content) {
        return new WinRateResponse(true, items, compareTarget, content, null);
    }

    public static WinRateResponse teamNameIsNull(String message) {
        return new WinRateResponse(false, List.of(), null, null, message);
    }
}
