package org.example.oddventure.domain.bet.repository;

import org.example.oddventure.domain.bet.entity.Bet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BetRepository extends JpaRepository<Bet, Long> {
}
