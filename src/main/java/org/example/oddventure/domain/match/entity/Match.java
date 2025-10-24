package org.example.oddventure.domain.match.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.oddventure.common.entity.BaseEntity;
import org.example.oddventure.domain.match.enums.MatchStatus;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "`match`")
public class Match extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long fetchId;

    @Column(nullable = false)
    private String matchName;

    @Column(nullable = false)
    private String teamA;

    @Column(nullable = false)
    private String teamB;

    @Column(nullable = false)
    private BigDecimal totalAmountA;

    @Column(nullable = false)
    private BigDecimal totalAmountB;

    @Column(nullable = false)
    private LocalDateTime startTime;

    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MatchStatus status;

    private String winner;

    private String loser;

    @Column(nullable = false)
    private Long viewCount = 0L;

    @Builder
    public Match(String matchName, String teamA, String teamB, LocalDateTime startTime) {
        this.matchName = matchName;
        this.teamA = teamA;
        this.teamB = teamB;
        this.startTime = startTime;
        this.status = MatchStatus.SCHEDULED;
        this.winner = null;
        this.loser = null;
        this.totalAmountA = BigDecimal.ZERO;
        this.totalAmountB = BigDecimal.ZERO;
        this.viewCount = 0L;
    }

    public void update(String matchName, String teamA, String teamB, LocalDateTime startTime, MatchStatus status) {
        if (matchName != null) {
            this.matchName = matchName;
        }
        if (teamA != null) {
            this.teamA = teamA;
        }
        if (teamB != null) {
            this.teamB = teamB;
        }
        if (startTime != null) {
            this.startTime = startTime;
        }
        if (status != null) {
            this.status = status;
        }
    }

    public void plusTeamA(BigDecimal amount) {
        this.totalAmountA = this.totalAmountA.add(amount);
    }

    public void plusTeamB(BigDecimal amount) {
        this.totalAmountB = this.totalAmountB.add(amount);
    }

    public void minusTeamA(BigDecimal amount) {
        this.totalAmountA = this.totalAmountA.subtract(amount);
    }

    public void minusTeamB(BigDecimal amount) {
        this.totalAmountB = this.totalAmountB.subtract(amount);
    }
}
