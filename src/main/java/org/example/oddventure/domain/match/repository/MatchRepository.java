package org.example.oddventure.domain.match.repository;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.example.oddventure.domain.match.entity.Match;
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
}
