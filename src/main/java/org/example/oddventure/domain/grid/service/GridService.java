package org.example.oddventure.domain.grid.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.oddventure.common.config.WebClientConfig;
import org.example.oddventure.domain.grid.dto.response.MatchFetchResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class GridService {

    private static final String GRID_CENTRAL_DATA_QUERY = """
            query GetAllSeriesInNext24Hours($after: Cursor) {
              allSeries(
                first: 10
                after: $after
                filter:{
                  titleId: "28" # CS2
                  startTimeScheduled:{
                    gte: "2025-10-24T15:00:07+02:00"
                    lte: "2025-10-25T15:00:07+02:00"
                  }
                }
                orderBy: StartTimeScheduled
              ) {
                totalCount,
                pageInfo{
                  hasPreviousPage
                  hasNextPage
                  startCursor
                  endCursor
                }
                edges{
                  cursor
                  node{
                    ...seriesFields
                  }
                }
              }
            }
            fragment seriesFields on Series {
              id
              title {
                nameShortened
              }
              tournament {
                nameShortened
              }
              startTimeScheduled
              format {
                nameShortened
              }
              teams {
                baseInfo {
                  name
                }
              }
            }
            """;
    private final WebClientConfig webClientConfig;
    private final ObjectMapper objectMapper;

    public List<MatchFetchResponse> fetchMatches() {
        List<MatchFetchResponse> results = new ArrayList<>();
        Map<String, Object> variables = new HashMap<>();

        String cursor = null; // 페이지네이션 커서
        while (true) {
            variables.put("after", cursor);
            String request;
            try {
                request = objectMapper.writeValueAsString(
                        Map.of("query", GRID_CENTRAL_DATA_QUERY,
                                "variables", variables
                        )
                );
//                log.info("Fetching matches from GRID: {}", request);
            } catch (JsonProcessingException e) {
                return results;
            }

            JsonNode response = webClientConfig.gridCentralClient().post()
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();
//            log.info("Got response from GRID central client: {}", response);
            if (response == null || response.path("data").isMissingNode()) {
                return results;
            }

            JsonNode allSeries = response.path("data").path("allSeries");

            for (JsonNode edge : allSeries.path("edges")) {
                JsonNode node = edge.path("node");
                Long fetchId = node.path("id").asLong();
                String teamA = node.path("teams").get(0).path("baseInfo").path("name").asText();
                String teamB = node.path("teams").get(1).path("baseInfo").path("name").asText();
                LocalDateTime startTime = Instant.parse(node.path("startTimeScheduled").asText())
                        .atZone(ZoneId.of("Asia/Seoul"))
                        .toLocalDateTime();

                results.add(MatchFetchResponse.builder()
                        .fetchId(fetchId)
                        .matchName(node.path("tournament").path("nameShortened").asText())
                        .teamA(teamA)
                        .teamB(teamB)
                        .startTime(startTime)
                        .build());
            }
            if (!allSeries.path("pageInfo").path("hasNextPage").asBoolean()) {
                break;
            }

            cursor = allSeries.path("pageInfo").path("endCursor").asText();
        }

        return results;
    }
}
