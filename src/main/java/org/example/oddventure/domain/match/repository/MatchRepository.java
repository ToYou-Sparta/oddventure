package org.example.oddventure.domain.match.repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.example.oddventure.domain.match.entity.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MatchRepository extends JpaRepository<Match, Long> {

    @Modifying
    @Query("UPDATE Match m SET m.viewCount = m.viewCount + 1 WHERE m.id = :id")
    void incrementViewCount(@Param("id") Long matchId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from Match m where m.id = :id")
    Optional<Match> findByIdForUpdate(@Param("id") Long id);

    // TODO: 검색 기능 작업 때 구현 예정
//    List<Match> findByConditions(String teamName, MatchStatus status);
}
