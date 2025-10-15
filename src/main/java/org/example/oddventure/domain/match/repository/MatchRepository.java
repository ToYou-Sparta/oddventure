package org.example.oddventure.domain.match.repository;

import org.example.oddventure.domain.match.entity.Match;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchRepository extends JpaRepository<Match, Long> {

    // TODO: 검색 기능 작업 때 구현 예정
//    List<Match> findByConditions(String teamName, MatchStatus status);
}
