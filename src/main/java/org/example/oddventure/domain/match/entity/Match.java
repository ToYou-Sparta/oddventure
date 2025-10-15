package org.example.oddventure.domain.match.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.oddventure.common.entity.BaseEntity;
import org.example.oddventure.domain.match.enums.MatchStatus;
import org.example.oddventure.domain.match.enums.MatchWinner;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Match extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String teamA;

    @Column(nullable = false, unique = true)
    private String teamB;

    @Column(nullable = false)
    private BigDecimal totalAmountA;

    @Column(nullable = false)
    private BigDecimal totalAmountB;

    @Column(nullable = false)
    private LocalDateTime startTime;

    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    private MatchStatus status;

    @Enumerated(EnumType.STRING)
    private MatchWinner winner;

    @Builder
    public Match(String teamA, String teamB, LocalDateTime startTime) {
        this.teamA = teamA;
        this.teamB = teamB;
        this.startTime = startTime;
        this.status = MatchStatus.SCHEDULED;
        this.winner = MatchWinner.NO_MATCH;
        this.totalAmountA = BigDecimal.ZERO;
        this.totalAmountB = BigDecimal.ZERO;
    }

    public void update(String teamA, String teamB, LocalDateTime startTime, MatchStatus status) {
        this.teamA = teamA;
        this.teamB = teamB;
        this.startTime = startTime;
        this.status = status;
    }
}
