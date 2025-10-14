package org.example.oddventure.match.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.oddventure.common.entity.BaseEntity;
import org.example.oddventure.match.enums.MatchStatus;
import org.example.oddventure.match.enums.MatchWinner;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Match extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String teamA;

    private String teamB;

    private BigDecimal amountTeamA;

    private BigDecimal amountTeamB;

    private LocalDate startTime;

    private LocalDate endTime;

    private MatchStatus status;

    private MatchWinner winner;

    @Builder
    public Match(String teamA, String teamB, LocalDate startTime) {
        this.teamA = teamA;
        this.teamB = teamB;
        this.startTime = startTime;
        this.status = MatchStatus.SCHEDULED;
        this.winner = MatchWinner.NO_MATCH;
        this.amountTeamA = BigDecimal.ZERO;
        this.amountTeamB = BigDecimal.ZERO;
    }
}
