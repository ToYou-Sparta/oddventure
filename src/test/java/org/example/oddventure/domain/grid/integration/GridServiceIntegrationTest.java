package org.example.oddventure.domain.grid.integration;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.example.oddventure.common.config.WebClientConfig;
import org.example.oddventure.domain.grid.dto.MatchResultDto;
import org.example.oddventure.domain.grid.dto.MatchScheduleDto;
import org.example.oddventure.domain.grid.service.GridService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;

public class GridServiceIntegrationTest {

    private MockWebServer mockCentralServer;
    private MockWebServer mockLiveServer;
    private GridService gridService;

    @BeforeEach
    void setUp() throws Exception {
        mockCentralServer = new MockWebServer();
        mockLiveServer = new MockWebServer();
        mockCentralServer.start();
        mockLiveServer.start();

        WebClientConfig webClientConfig = new WebClientConfig();

        ReflectionTestUtils.setField(webClientConfig, "GRID_CENTRAL_BASE_URL", mockCentralServer.url("/").toString());
        ReflectionTestUtils.setField(webClientConfig, "GRID_LIVE_BASE_URL", mockLiveServer.url("/").toString());
        ReflectionTestUtils.setField(webClientConfig, "GRID_API_KEY", "test-key");

        gridService = new GridService(webClientConfig, new ObjectMapper());
    }

    @AfterEach
    void setDown() throws Exception {
        mockCentralServer.shutdown();
        mockLiveServer.shutdown();
    }

    @Test
    @DisplayName("fetchMatchSchedules() 통합 테스트 - 경기 스케줄 목록 조회 성공")
    void fetchMatchSchedules_success() {
        // given
        String jsonResponse = """
                {
                  "data": {
                    "allSeries": {
                      "edges": [
                        {
                          "cursor": "abc",
                          "node": {
                            "id": "1001",
                            "tournament": {"nameShortened": "LCK"},
                            "startTimeScheduled": "2025-11-03T16:00:00Z",
                            "teams": [
                              {"baseInfo": {"name": "T1"}},
                              {"baseInfo": {"name": "GEN.G"}}
                            ]
                          }
                        }
                      ],
                      "pageInfo": {
                        "hasNextPage": false,
                        "endCursor": "abc"
                      }
                    }
                  }
                }
                """;

        mockCentralServer.enqueue(new MockResponse()
                .setBody(jsonResponse)
                .addHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE));

        // when
        List<MatchScheduleDto> result = gridService.fetchMatchSchedules();

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).fetchId()).isEqualTo(1001L);
        assertThat(result.get(0).matchName()).isEqualTo("LCK");
        assertThat(result.get(0).teamA()).isEqualTo("T1");
        assertThat(result.get(0).teamB()).isEqualTo("GEN.G");
    }

    @Test
    @DisplayName("fetchMatchResult() 통합 테스트 - 경기 결과 조회 성공")
    void fetchMatchResult_success() {
        // given
        String jsonResponse = """
                {
                  "data": {
                    "seriesState": {
                      "finished": true,
                      "teams": [
                        {"name": "T1", "won": true},
                        {"name": "GEN.G", "won": false}
                      ]
                    }
                  }
                }
                """;

        mockLiveServer.enqueue(new MockResponse()
                .setBody(jsonResponse)
                .addHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE));

        Long fetchId = 1001L;

        // when
        MatchResultDto result = gridService.fetchMatchResult(fetchId);

        // then
        assertThat(result.fetchId()).isEqualTo(fetchId);
        assertThat(result.finished()).isTrue();
        assertThat(result.winner()).isEqualTo("T1");
        assertThat(result.loser()).isEqualTo("GEN.G");
    }
}
