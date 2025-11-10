package org.example.oddventure.domain.ai.tools;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.oddventure.domain.ai.dto.Cs2NewsItem;
import org.example.oddventure.domain.ai.dto.SteamNewsResponse;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class Cs2NewsTools {

    private static final String STEAM_NEWS_PATH =
            "/ISteamNews/GetNewsForApp/v2/?appid=730&count=%d&maxlength=500&format=json";

    private final RestClient steamClient;

    @Tool(
            name = "query_cs2_news",
            description = "Counter-Strike 2 관련 최신 뉴스 5개를 조회한다. (Steam 공식 API 기반)"
    )
    public List<Cs2NewsItem> queryCs2News() {
        int count = 5;
        SteamNewsResponse response = steamClient.get()
                .uri(STEAM_NEWS_PATH, count)
                .retrieve()
                .body(SteamNewsResponse.class);

        if (response == null || response.appnews() == null) {
            throw new IllegalStateException("Steam API 응답이 비어있습니다.");
        }

        return response.appnews().newsitems();
    }
}
