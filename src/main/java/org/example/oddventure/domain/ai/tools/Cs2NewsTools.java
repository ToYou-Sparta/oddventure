package org.example.oddventure.domain.ai.tools;

import io.github.resilience4j.retry.annotation.Retry;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.oddventure.domain.ai.dto.Cs2NewsItem;
import org.example.oddventure.domain.ai.dto.SteamNewsResponse;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
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

        try {
            SteamNewsResponse response = fetchNewsWithRetry(count);

            if (response == null || response.appnews() == null || response.appnews().newsitems() == null
                    || response.appnews().newsitems().isEmpty()) {
                log.warn("Steam API 응답 필드 누락 → fallback 반환");
                return fallbackNews();
            }

            return response.appnews().newsitems();
        } catch (Exception e) {
            return fallbackNews();
        }
    }

    @Retry(name = "steamNews", fallbackMethod = "fetchNewsFallback")
    public SteamNewsResponse fetchNewsWithRetry(int count) {
        return steamClient.get()
                .uri(STEAM_NEWS_PATH, count)
                .retrieve()
                .body(SteamNewsResponse.class);
    }

    public SteamNewsResponse fetchNewsFallback(int count, Throwable t) {
        log.warn("Retry 실패 → fetchNewsFallback 실행. 원인: {}", t.getMessage());
        return null;
    }

    private List<Cs2NewsItem> fallbackNews() {
        return List.of(
                new Cs2NewsItem(
                        "CS2 최신 뉴스 정보를 가져올 수 없습니다.",
                        "https://store.steampowered.com/app/730/CounterStrike_2", // Steam 공식 CS2 페이지
                        "잠시 후 다시 시도해주세요.",
                        System.currentTimeMillis()
                )
        );
    }
}
