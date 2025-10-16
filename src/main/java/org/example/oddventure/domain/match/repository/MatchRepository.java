package org.example.oddventure.domain.match.repository;

import org.example.oddventure.domain.match.entity.Match;
import org.example.oddventure.domain.match.enums.MatchWinner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MatchRepository extends JpaRepository<Match, Long> {

    @Modifying
    @Query("UPDATE Match m SET m.viewCount = m.viewCount + 1 WHERE m.id = :id")
    void incrementViewCount(@Param("id") Long matchId);

    // TODO: 검색 기능 작업 때 구현 예정
//    List<Match> findByConditions(String teamName, MatchStatus status);

    @Query("SELECT m FROM Match m WHERE m.winner = :TEAM_A AND m.winner = :TEAM_B")
    List<Match> findAllByWinner();
}
