package org.example.oddventure.bet.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.oddventure.bet.enums.SelectedTeam;
import org.example.oddventure.common.entity.BaseEntity;

import java.math.BigDecimal;

@Entity
@Getter
@NoArgsConstructor
public class Bet extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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
