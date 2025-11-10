package org.example.oddventure.domain.match.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.example.oddventure.domain.match.entity.Match;
import org.example.oddventure.domain.match.enums.MatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface MatchRepository extends JpaRepository<Match, Long>, MatchRepositoryCustom {

    // 스케줄러가 Redis의 조회수 총합을 DB에 덮어쓰기(동기화)하기 위한 쿼리
    @Transactional
    @Modifying
    @Query("UPDATE Match m SET m.viewCount = :count WHERE m.id = :id")
    int updateViewCount(@Param("id") Long matchId, @Param("count") Long count);

    @Query("select m.winner from Match m where m.winner is not null")
    List<String> findByWinnerIsNotNull();

    @Query("select m.loser from Match m where m.loser is not null")
    List<String> findByLoserIsNotNull();

    boolean existsByFetchId(Long fetchId);

    Optional<Match> findByFetchId(Long fetchId);

    @Query("""
            select m
            from Match m
            where m.status = :status
            and m.startTime < CURRENT_TIMESTAMP
            and m.startTime >= :twoDaysAgo
            """)
    List<Match> findByMatchStatus(@Param("status") MatchStatus matchStatus,
                                  @Param("twoDaysAgo") LocalDateTime twoDaysAgo);
}
