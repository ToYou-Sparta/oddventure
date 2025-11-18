package org.example.oddventure.domain.match.service;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.oddventure.domain.hotKeywords.service.HotKeywordsService;
import org.example.oddventure.domain.match.document.MatchDocument;
import org.example.oddventure.domain.match.dto.request.MatchSearchCondition;
import org.example.oddventure.domain.match.dto.response.MatchResponse;
import org.example.oddventure.domain.match.enums.MatchStatus;
import org.example.oddventure.domain.match.repository.elasticsearch.MatchSearchRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchSearchService {

    private final MatchSearchRepository matchSearchRepository;
    private final ElasticsearchOperations elasticsearchOperations;
    private final HotKeywordsService hotKeywordsService;

    public Page<MatchResponse> searchMatches(MatchSearchCondition condition, Pageable pageable) {
        log.info("Elasticsearch 검색 시작 - keyword: {}, fromDate: {}, toDate{}",
                condition.keyword(), condition.fromDate(), condition.toDate());

        // 키워드가 있으면 인기 검색어에 추가
        if (condition.keyword() != null && !condition.keyword().isBlank()) {
            hotKeywordsService.incrementSearchScore(condition.keyword());
        }

        // 검색 쿼리 빌드
        NativeQuery searchQuery = buildSearchQuery(condition, pageable);

        //Elasticsearch 검색 실행
        SearchHits<MatchDocument> searchHits = elasticsearchOperations.search(
                searchQuery,
                MatchDocument.class
        );

        // 검색 결과를 MatchResponse로 변환
        List<MatchResponse> matchResponses = searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .map(this::toMatchResponse)
                .toList();

        return new PageImpl<>(matchResponses, pageable, searchHits.getTotalHits());
    }

    /**
     * Elasticsearch NativeQuery 빌드
     */
    private NativeQuery buildSearchQuery(MatchSearchCondition condition, Pageable pageable) {
        List<Query> mustQueries = new ArrayList<>();

        // 1. 키워드 검색 (matchName, teamA, teamB)
        if (condition.keyword() != null && !condition.keyword().isBlank()) {
            Query keywordQuery = Query.of(q -> q
                    .bool(BoolQuery.of(b -> b
                            .should(Query.of(s -> s
                                    .match(m -> m
                                            .field("matchName")
                                            .query(condition.keyword())
                                            .boost(2.0f)  // 매치 이름 가중치 높임
                                    )
                            ))
                            .should(Query.of(s -> s
                                    .match(m -> m
                                            .field("teamA")
                                            .query(condition.keyword())
                                            .boost(1.5f)
                                    )
                            ))
                            .should(Query.of(s -> s
                                    .match(m -> m
                                            .field("teamB")
                                            .query(condition.keyword())
                                            .boost(1.5f)
                                    )
                            ))
                            .minimumShouldMatch("1")  // 최소 1개 조건 만족
                    ))
            );
            mustQueries.add(keywordQuery);
        }

        // 2. 날짜 범위 필터
        if (condition.fromDate() != null || condition.toDate() != null) {
            RangeQuery dateRangeQuery = new RangeQuery.Builder()
                    .date(d -> {
                        d.field("startTime");
                        if (condition.fromDate() != null) {
                            d.gte(formatDateTime(condition.fromDate()));
                        }
                        if (condition.toDate() != null) {
                            d.lte(formatDateTime(condition.toDate()));
                        }
                        return d;
                    })
                    .build();

            mustQueries.add(dateRangeQuery._toQuery());
        }

        // 3. BoolQuery로 조합
        BoolQuery boolQuery = BoolQuery.of(b -> {
            mustQueries.forEach(b::must);
            return b;
        });

        // 4. NativeQuery 생성
        return NativeQuery.builder()
                .withQuery(Query.of(q -> q.bool(boolQuery)))
                .withPageable(pageable)
                .build();
    }

    /**
     * LocalDateTime을 Elasticsearch 날짜 형식으로 변환 date_hour_minute_second 포맷에 맞춰 밀리초 제거
     */
    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
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