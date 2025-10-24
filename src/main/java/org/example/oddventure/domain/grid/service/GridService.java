package org.example.oddventure.domain.grid.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.example.oddventure.common.config.WebClientConfig;
import org.example.oddventure.domain.grid.dto.response.MatchFetchResponse;
import org.springframework.stereotype.Service;

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
        String cursor = null;
        List<MatchFetchResponse> results = new ArrayList<>();

        while (true) {
            String request;
            try {
                request = objectMapper.writeValueAsString(
                        Map.of("query", GRID_CENTRAL_DATA_QUERY,
                                "variables", Map.of("after", cursor)
                        )
                );
            } catch (JsonProcessingException e) {
                return results;
            }

            JsonNode response = webClientConfig.gridCentralClient().post()
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .onErrorReturn(null)
                    .block();

            if (response == null || response.path("data").isMissingNode()) {
                return results;
            }

            JsonNode allSeries = response.path("data").path("allSeries");

            for (JsonNode edge : allSeries.path("edges")) {
                JsonNode node = edge.path("node");
                String teamA = node.path("teams").get(0).path("baseInfo").path("name").asText();
                String teamB = node.path("teams").get(1).path("baseInfo").path("name").asText();

                results.add(MatchFetchResponse.builder()
                        .matchName(node.path("tournament").path("nameShortened").asText())
                        .teamA(teamA)
                        .teamB(teamB)
                        .startTime(LocalDateTime.parse(node.path("startTimeScheduled").asText()))
                        .build());
            }
            boolean hasNext = allSeries.path("pageInfo").path("hasNextPage").asBoolean();
            if (!hasNext) {
                break;
            }

            cursor = allSeries.path("pageInfo").path("endCursor").asText();
        }

        return results;
    }
}
