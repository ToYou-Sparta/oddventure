package org.example.oddventure.domain.bet.repository;

import java.util.Optional;
import org.example.oddventure.domain.bet.entity.Bet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BetRepository extends JpaRepository<Bet, Long> {

    @EntityGraph(attributePaths = {"match"})
    Page<Bet> findByUserId(Long userId, Pageable pageable);

    @EntityGraph(attributePaths = {"match", "user"})
    @Query("select b from Bet b where b.id = :id")
    Optional<Bet> findByIdForDelete(Long id);
}