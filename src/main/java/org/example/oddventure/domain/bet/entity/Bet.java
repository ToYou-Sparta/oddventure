package org.example.oddventure.domain.bet.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.oddventure.common.entity.BaseEntity;
import org.example.oddventure.domain.bet.enums.SelectedTeam;
import org.example.oddventure.domain.match.entity.Match;
import org.example.oddventure.domain.user.entity.User;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Bet extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false)
    private Match match;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SelectedTeam selectedTeam;

    @Column(nullable = false)
    private BigDecimal betAmount;

    @Column(nullable = false)
    private BigDecimal oddsAtBetting;

    @Column(name = "is_win", nullable = false)
    private boolean isWin;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @Builder
    public Bet(User user, Match match, SelectedTeam selectedTeam, BigDecimal betAmount, BigDecimal oddsAtBetting,
               boolean isWin) {
        this.user = user;
        this.match = match;
        this.selectedTeam = selectedTeam;
        this.betAmount = betAmount;
        this.oddsAtBetting = oddsAtBetting;
        this.isWin = isWin;
    }

    public static Bet create(User user, Match match, BigDecimal odds, SelectedTeam selectedTeam, BigDecimal betAmount) {
        return Bet.builder()
                .user(user)
                .match(match)
                .oddsAtBetting(odds)
                .selectedTeam(selectedTeam)
                .betAmount(betAmount)
                .isWin(false)
                .build();
    }

    public void setDeleted(boolean isDeleted) {
        this.isDeleted = isDeleted;
    }
}
