package org.example.oddventure.domain.match.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.example.oddventure.domain.hotKeywords.service.HotKeywordsService;
import org.example.oddventure.domain.match.document.MatchDocument;
import org.example.oddventure.domain.match.dto.request.MatchSearchCondition;
import org.example.oddventure.domain.match.dto.response.MatchResponse;
import org.example.oddventure.domain.match.enums.MatchStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchSearchService {

    private final RestHighLevelClient elasticsearchClient;
    private final HotKeywordsService hotKeywordsService;
    private final ObjectMapper objectMapper;

    public Page<MatchResponse> searchMatches(MatchSearchCondition condition, Pageable pageable) {
        log.info("Elasticsearch 검색 시작 - keyword: {}, fromDate: {}, toDate{}",
                condition.keyword(), condition.fromDate(), condition.toDate());

        // 키워드가 있으면 인기 검색어에 추가
        if (condition.keyword() != null && !condition.keyword().isBlank()) {
            hotKeywordsService.incrementSearchScore(condition.keyword());
        }

        try {
            // 검색 쿼리 빌드
            SearchSourceBuilder searchSourceBuilder = buildSearchQuery(condition, pageable);

            // Elasticsearch 검색 실행
            SearchRequest searchRequest = new SearchRequest("matches"); // 인덱스 이름
            searchRequest.source(searchSourceBuilder);

            SearchResponse searchResponse = elasticsearchClient.search(searchRequest, RequestOptions.DEFAULT);

            // 검색 결과를 MatchResponse로 변환
            List<MatchResponse> matchResponses = new ArrayList<>();
            for (SearchHit hit : searchResponse.getHits().getHits()) {
                MatchDocument document = objectMapper.readValue(hit.getSourceAsString(), MatchDocument.class);
                matchResponses.add(toMatchResponse(document));
            }

            long totalHits = searchResponse.getHits().getTotalHits().value;
            return new PageImpl<>(matchResponses, pageable, totalHits);

        } catch (IOException e) {
            log.error("Elasticsearch 검색 중 오류 발생", e);
            throw new RuntimeException("검색 중 오류가 발생했습니다", e);
        }
    }

    /**
     * Elasticsearch SearchSourceBuilder 빌드
     */
    private SearchSourceBuilder buildSearchQuery(MatchSearchCondition condition, Pageable pageable) {
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

        // 1. 키워드 검색 (matchName, teamA, teamB)
        if (condition.keyword() != null && !condition.keyword().isBlank()) {
            BoolQueryBuilder keywordQuery = QueryBuilders.boolQuery()
                    .should(QueryBuilders.matchQuery("matchName", condition.keyword()).boost(2.0f))
                    .should(QueryBuilders.matchQuery("teamA", condition.keyword()).boost(1.5f))
                    .should(QueryBuilders.matchQuery("teamB", condition.keyword()).boost(1.5f))
                    .minimumShouldMatch(1);  // 최소 1개 조건 만족

            boolQuery.must(keywordQuery);
        }

        // 2. 날짜 범위 필터
        if (condition.fromDate() != null || condition.toDate() != null) {
            RangeQueryBuilder dateRangeQuery = QueryBuilders.rangeQuery("startTime");

            if (condition.fromDate() != null) {
                dateRangeQuery.gte(formatDateTime(condition.fromDate()));
            }
            if (condition.toDate() != null) {
                dateRangeQuery.lte(formatDateTime(condition.toDate()));
            }

            boolQuery.must(dateRangeQuery);
        }

        // 3. SearchSourceBuilder로 쿼리 + 페이징 설정
        return new SearchSourceBuilder()
                .query(boolQuery)
                .from((int) pageable.getOffset())
                .size(pageable.getPageSize());
    }

    /**
     * LocalDateTime을 Elasticsearch 날짜 형식으로 변환
     */
    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    /**
     * MatchDocument를 MatchResponse로 변환
     */
    private MatchResponse toMatchResponse(MatchDocument document) {
        return new MatchResponse(
                Long.parseLong(document.getId()),
                document.getMatchName(),
                document.getTeamA(),
                document.getTeamB(),
                document.getTotalAmountA(),
                document.getTotalAmountB(),
                document.getStartTime(),
                document.getEndTime(),
                MatchStatus.valueOf(document.getStatus()),
                document.getWinner(),
                document.getLoser(),
                document.getViewCount(),
                document.getCreatedAt()
        );
    }
}