package org.example.oddventure.domain.bet.repository;

import org.example.oddventure.domain.bet.entity.Bet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BetRepository extends JpaRepository<Bet, Long> {
    Page<Bet> findByUserId(Long userId, Pageable pageable);
}
