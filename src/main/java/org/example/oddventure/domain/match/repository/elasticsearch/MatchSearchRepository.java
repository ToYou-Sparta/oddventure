package org.example.oddventure.domain.match.repository.elasticsearch;

import org.example.oddventure.domain.match.document.MatchDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

/**
 * Elasticsearch 검색용 Repository
 */
@Repository
public interface MatchSearchRepository extends ElasticsearchRepository<MatchDocument, String> {

    // 기본 CRUD 메서드 명시적 선언 (버전 호환성 문제 해결)
    <S extends MatchDocument> S save(S entity);

    <S extends MatchDocument> Iterable<S> saveAll(Iterable<S> entities);

    void deleteById(String id);

    /**
     * 키워드로 매치 이름, 팀A, 팀B에서 검색
     * Query DSL을 JSON 형태로 직접 작성
     */
    @Query("""
        {
          "bool": {
            "should": [
              {"match": {"matchName": {"query": "?0", "boost": 2.0}}},
              {"match": {"teamA": {"query": "?0", "boost": 1.5}}},
              {"match": {"teamB": {"query": "?0", "boost": 1.5}}}
            ],
            "minimum_should_match": 1
          }
        }
        """)
    Page<MatchDocument> searchByKeyword(String keyword, Pageable pageable);

    /**
     * 팀 이름으로 정확히 일치하는 매치 검색
     */
    Page<MatchDocument> findByTeamAOrTeamB(String teamA, String teamB, Pageable pageable);
}