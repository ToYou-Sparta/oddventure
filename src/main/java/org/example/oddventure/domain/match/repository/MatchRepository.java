package org.example.oddventure.domain.match.repository;

import org.example.oddventure.domain.match.entity.Match;
import org.example.oddventure.domain.match.enums.MatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MatchRepository extends JpaRepository<Match, Long> {

    // TODO: JPQL로 구현
    List<Match> findByConditions(String teamName, MatchStatus status);
}
