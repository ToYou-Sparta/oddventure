package org.example.oddventure.domain.ai.tools;

import lombok.RequiredArgsConstructor;
import org.example.oddventure.domain.hotKeywords.dto.HotKeywordsResponse;
import org.example.oddventure.domain.hotKeywords.service.HotKeywordsService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HotKeywordTools {

    private final HotKeywordsService hotKeywordsService;

    @Tool(
            name = "query_hot_keywords",
            description = "최근 인기 있는 e-sports 관련 검색 키워드를 조회한다. (팀명, 리그명 등)"
    )
    public HotKeywordsResponse queryHotKeywords() {
        return hotKeywordsService.getHotKeywords();
    }
}
