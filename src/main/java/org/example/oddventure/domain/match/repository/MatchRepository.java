package org.example.oddventure.domain.match.repository;

import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.example.oddventure.domain.match.entity.Match;
import org.example.oddventure.domain.match.enums.MatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MatchRepository extends JpaRepository<Match, Long>, MatchRepositoryCustom {

    @Modifying
    @Query("UPDATE Match m SET m.viewCount = m.viewCount + 1 WHERE m.id = :id")
    int incrementViewCount(@Param("id") Long matchId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from Match m where m.id = :id")
    Optional<Match> findByIdForUpdate(@Param("id") Long id);

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
    
    @Query("select m.fetchId from Match m where m.fetchId in :fetchIds")
    List<Long> findExistingFetchIds(@Param("fetchIds") List<Long> allFetchIds);
}
