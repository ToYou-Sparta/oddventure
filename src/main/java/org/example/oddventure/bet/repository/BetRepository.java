package org.example.oddventure.bet.repository;

import org.example.oddventure.bet.entity.Bet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BetRepository extends JpaRepository<Bet, Long> {
}
