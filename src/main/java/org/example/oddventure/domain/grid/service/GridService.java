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
import org.example.oddventure.domain.grid.dto.MatchResultDto;
import org.example.oddventure.domain.grid.dto.MatchScheduleDto;
import org.example.oddventure.domain.grid.dto.response.AllSeriesResponse;
import org.example.oddventure.domain.grid.dto.response.field.Edge;
import org.example.oddventure.domain.grid.dto.response.field.Node;
import org.example.oddventure.domain.grid.dto.response.field.PageInfo;
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
    private static final String GRID_LIVE_DATA_QUERY = """
            query GetLiveCS2SeriesState ($id: ID!){
               seriesState(id: $id) {
                 teams {
                   name
                   won
                 }
               }
             }
            """;
    private final WebClientConfig webClientConfig;
    private final ObjectMapper objectMapper;

    public List<MatchScheduleDto> fetchMatchSchedules() {
        List<MatchScheduleDto> results = new ArrayList<>();
        Map<String, Object> variables = new HashMap<>();

        String cursor = null; // 페이지네이션 커서
        while (true) {
            variables.put("after", cursor);
            String request;
            try {
                request = objectMapper.writeValueAsString(
                        Map.of("query", GRID_CENTRAL_DATA_QUERY,
                                "variables", variables)
                );
//                log.info("Fetching matches from GRID: {}", request);
            } catch (JsonProcessingException e) {
                return results;
            }

            AllSeriesResponse response = webClientConfig.gridCentralClient().post()
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(AllSeriesResponse.class)
                    .block();
//            log.info("Got response from GRID central client: {}", response);
            if (response == null
                    || response.data() == null
                    || response.data().allSeries() == null
                    || response.data().allSeries().edges() == null) {
                log.warn("가져온 매치 스케쥴 응답값이 비어있습니다.");
                return results;
            }

            List<Edge> edges = response.data().allSeries().edges();

            for (Edge edge : edges) {
                Node node = edge.node();
                String matchName = node.tournament().nameShortened();
                Long fetchId = Long.parseLong(node.id());
                String teamA = node.teams().get(0).baseInfo().name();
                String teamB = node.teams().get(1).baseInfo().name();
                LocalDateTime startTime = Instant.parse(node.startTimeScheduled())
                        .atZone(ZoneId.of("Asia/Seoul"))
                        .toLocalDateTime();

                results.add(MatchScheduleDto.builder()
                        .fetchId(fetchId)
                        .matchName(matchName)
                        .teamA(teamA)
                        .teamB(teamB)
                        .startTime(startTime)
                        .build());
            }

            PageInfo pageInfo = response.data().allSeries().pageInfo();
            if (!pageInfo.hasNextPage()) {
                break;
            }

            cursor = pageInfo.endCursor();
        }

        return results;
    }

    public MatchResultDto fetchMatchResult(Long fetchId) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("id", fetchId.toString());
        String request = null;
        try {
            request = objectMapper.writeValueAsString(
                    Map.of("query", GRID_LIVE_DATA_QUERY,
                            "variables", variables)
            );
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize GraphQL request body.", e);
        }

        JsonNode response = webClientConfig.gridLiveClient().post()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();
//        log.info(response.toString());
        if (response == null || response.path("data").isMissingNode()) {
            throw new IllegalStateException("Response body is empty.");
        }

        JsonNode teams = response.path("data").path("seriesState").path("teams");
        String winner = null;
        String looser = null;

        for (JsonNode team : teams) {
            String name = team.path("name").asText();
            boolean won = team.path("won").asBoolean();

            if (won) {
                winner = name;
            } else {
                looser = name;
            }
        }

        return MatchResultDto.builder()
                .fetchId(fetchId)
                .winner(winner)
                .looser(looser)
                .build();
    }
}
