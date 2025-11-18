package org.example.oddventure.domain.ai.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.List;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.example.oddventure.domain.ai.dto.Cs2NewsItem;
import org.example.oddventure.domain.ai.tools.Cs2NewsTools;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;

@SpringBootTest
public class Cs2NewsToolsIntegrationTest {

    @Autowired
    Cs2NewsTools cs2NewsTools;
    private MockWebServer mockWebServer;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        RestClient testSteamClient = RestClient.builder()
                .baseUrl(mockWebServer.url("/").toString())
                .build();

        ReflectionTestUtils.setField(cs2NewsTools, "steamClient", testSteamClient);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    @DisplayName("Steam API 연속 실패 시 fallback 응답을 반환한다")
    void retry_and_fallback() {
        // given
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));

        // when
        List<Cs2NewsItem> items = cs2NewsTools.queryCs2News();

        // then
        assertThat(items).hasSize(1);
        assertThat(items.get(0).title()).contains("가져올 수 없습니다");
    }
}
