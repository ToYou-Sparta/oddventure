package org.example.oddventure.domain.bet.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import lombok.RequiredArgsConstructor;
import org.example.oddventure.domain.bet.dto.request.BetCreateRequest;
import org.example.oddventure.domain.bet.entity.Bet;
import org.example.oddventure.domain.bet.enums.SelectedTeam;
import org.example.oddventure.domain.bet.exception.BetErrorCode;
import org.example.oddventure.domain.bet.exception.BetException;
import org.example.oddventure.domain.bet.repository.BetRepository;
import org.example.oddventure.domain.match.entity.Match;
import org.example.oddventure.domain.match.enums.MatchStatus;
import org.example.oddventure.domain.match.exception.MatchErrorCode;
import org.example.oddventure.domain.match.exception.MatchException;
import org.example.oddventure.domain.match.repository.MatchRepository;
import org.example.oddventure.domain.user.entity.User;
import org.example.oddventure.domain.user.exception.UserErrorCode;
import org.example.oddventure.domain.user.exception.UserException;
import org.example.oddventure.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 베팅 관련 DB 트랜잭션만을 전담하는 서비스
 * 분산 락이 획득된 상태에서만 호출되어야 함
 */
@Service
@RequiredArgsConstructor
public class BetTransactionService {

    private final BetRepository betRepository;
    private final UserRepository userRepository;
    private final MatchRepository matchRepository;

    @Transactional
    public CreateBetData createBetInternal(Long userId, BetCreateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        BigDecimal betAmount = BigDecimal.valueOf(request.betAmount());

        if (user.getPoint().compareTo(betAmount) < 0) {
            throw new BetException(BetErrorCode.NOT_ENOUGH_POINTS);
        }

        Match match = matchRepository.findById(request.matchId())
                .orElseThrow(() -> new MatchException(MatchErrorCode.MATCH_NOT_FOUND));

        validateBettable(match);
        user.minusPoint(betAmount);
        BigDecimal odds = calculateOdds(match, request.selectedTeam());
        updateTotalAmount(match, betAmount, request.selectedTeam());

        Bet bet = request.toEntity(user, match, odds);
        betRepository.save(bet);

        String selectedTeamName = selectedTeamName(match, request.selectedTeam());

        return new CreateBetData(bet, selectedTeamName, user.getPoint(), odds);
    }

    @Transactional
    public DeleteBetData deleteBetInternal(Long userId, Long betId) {
        Bet bet = betRepository.findByIdForDelete(betId)
                .orElseThrow(() -> new BetException(BetErrorCode.BET_NOT_FOUND));

        if (!bet.getUser().getId().equals(userId)) {
            throw new BetException(BetErrorCode.PERMISSION_DENIED);
        }

        Match match = matchRepository.findById(bet.getMatch().getId())
                .orElseThrow(() -> new MatchException(MatchErrorCode.MATCH_NOT_FOUND));
        validateCancelable(match.getStatus());

        bet.delete();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
        user.plusPoint(bet.getBetAmount());

        refundTotalAmount(match, bet.getBetAmount(), bet.getSelectedTeam());

        return new DeleteBetData(bet, user);
    }

    private void validateBettable(Match match) {
        if (match.isDeleted()) {
            throw new BetException(BetErrorCode.MATCH_NOT_EXIST);
        }
        if (!match.getStatus().equals(MatchStatus.SCHEDULED)) {
            throw new BetException(BetErrorCode.MATCH_NOT_BETTABLE);
        }
    }

    private void validateCancelable(MatchStatus status) {
        if (!status.equals(MatchStatus.SCHEDULED)) {
            throw new BetException(BetErrorCode.MATCH_NOT_CANCELABLE);
        }
    }

    private void updateTotalAmount(Match match, BigDecimal amount, SelectedTeam selectedTeam) {
        if (selectedTeam.equals(SelectedTeam.Team_A)) {
            match.plusTeamA(amount);
        } else if (selectedTeam.equals(SelectedTeam.Team_B)) {
            match.plusTeamB(amount);
        }
    }

    private void refundTotalAmount(Match match, BigDecimal amount, SelectedTeam selectedTeam) {
        if (selectedTeam.equals(SelectedTeam.Team_A)) {
            match.minusTeamA(amount);
        } else if (selectedTeam.equals(SelectedTeam.Team_B)) {
            match.minusTeamB(amount);
        }
    }

    private BigDecimal calculateOdds(Match match, SelectedTeam selectedTeam) {
        BigDecimal total = match.getTotalAmountA().add(match.getTotalAmountB());
        System.out.println("total: " + total);
        BigDecimal probability;

        if (selectedTeam.equals(SelectedTeam.Team_A)) {
            probability = match.getTotalAmountA().divide(total, 2, RoundingMode.HALF_UP);
        } else {
            probability = match.getTotalAmountB().divide(total, 2, RoundingMode.HALF_UP);
        }

        System.out.println("probability: " + probability);
        return new BigDecimal("0.9").divide(probability, 2, RoundingMode.HALF_UP);
    }

    private String selectedTeamName(Match match, SelectedTeam selectedTeam) {
        if (selectedTeam.equals(SelectedTeam.Team_A)) {
            return match.getTeamA();
        } else {
            return match.getTeamB();
        }
    }

    // 트랜잭션 외부로 데이터를 전달하기 위한 내부 DTO
    public record CreateBetData(Bet bet, String selectedTeamName, BigDecimal userPointAfter, BigDecimal odds) {}
    public record DeleteBetData(Bet bet, User user) {}
}