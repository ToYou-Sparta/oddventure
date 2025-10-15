package org.example.oddventure.bet.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
import org.example.oddventure.bet.enums.SelectedTeam;
import org.example.oddventure.common.entity.BaseEntity;
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

    @Column(nullable = false)
    private SelectedTeam selectedTeam;

    @Column(nullable = false)
    private BigDecimal betAmount;

    @Column(nullable = false)
    private BigDecimal oddsAtBetting;

    @Column(nullable = false)
    private boolean isWin;

    @Builder
    public Bet(SelectedTeam selectedTeam, BigDecimal betAmount, BigDecimal oddsAtBetting, Boolean isWin) {
        this.selectedTeam = selectedTeam;
        this.betAmount = betAmount;
        this.oddsAtBetting = oddsAtBetting;
        this.isWin = isWin;
    }

}
