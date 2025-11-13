package org.example.oddventure.domain.ai.unit.tools;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.List;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.example.oddventure.domain.ai.dto.Cs2NewsItem;
import org.example.oddventure.domain.ai.tools.Cs2NewsTools;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

public class Cs2NewsToolsTest {

    private MockWebServer mockWebServer;
    private Cs2NewsTools cs2NewsTools;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        // 테스트용 RestClient: baseUrl을 MockWebServer로 설정
        RestClient testSteamClient = RestClient.builder()
                .baseUrl(mockWebServer.url("/").toString())
                .build();

        cs2NewsTools = new Cs2NewsTools(testSteamClient);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void queryCs2News_success() {
        String body = """
                { "appnews": { "newsitems": [
                   {"title":"CS2 업데이트 안내","url":"https://store.steampowered.com/news/1","contents":"패치","date":12345},
                   {"title":"무기 밸런스 조정","url":"https://store.steampowered.com/news/2","contents":"AK-47","date":12346}
                ]}}""";
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody(body));

        List<Cs2NewsItem> items = cs2NewsTools.queryCs2News();

        assertThat(items).hasSize(2);
        assertThat(items.get(0).title()).contains("업데이트");
    }
}
