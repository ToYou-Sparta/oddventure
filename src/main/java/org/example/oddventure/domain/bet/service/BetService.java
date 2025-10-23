package org.example.oddventure.domain.bet.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import lombok.RequiredArgsConstructor;
import org.example.oddventure.domain.bet.dto.request.BetCreateRequest;
import org.example.oddventure.domain.bet.dto.response.BetCreateResponse;
import org.example.oddventure.domain.bet.dto.response.BetDeleteResponse;
import org.example.oddventure.domain.bet.dto.response.BetResponse;
import org.example.oddventure.domain.bet.entity.Bet;
import org.example.oddventure.domain.bet.enums.SelectedTeam;
import org.example.oddventure.domain.bet.exception.BetErrorCode;
import org.example.oddventure.domain.bet.exception.InvalidBetException;
import org.example.oddventure.domain.bet.repository.BetRepository;
import org.example.oddventure.domain.match.entity.Match;
import org.example.oddventure.domain.match.enums.MatchStatus;
import org.example.oddventure.domain.match.exception.MatchErrorCode;
import org.example.oddventure.domain.match.exception.MatchException;
import org.example.oddventure.domain.match.repository.MatchRepository;
import org.example.oddventure.domain.user.entity.User;
import org.example.oddventure.domain.user.exception.InvalidUserException;
import org.example.oddventure.domain.user.exception.UserErrorCode;
import org.example.oddventure.domain.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BetService {

    private final BetRepository betRepository;
    private final UserRepository userRepository;
    private final MatchRepository matchRepository;

    @Transactional
    public BetCreateResponse createBet(Long userId, BetCreateRequest request) {
        User user = findUserById(userId);

        BigDecimal betAmount = BigDecimal.valueOf(request.betAmount());

        // 잔액 부족
        if (user.getPoint().compareTo(betAmount) < 0) {
            throw new InvalidBetException(BetErrorCode.NOT_ENOUGH_POINTS);
        }

        Match match = matchRepository.findByIdForUpdate(request.matchId())
                .orElseThrow(() -> new MatchException(MatchErrorCode.MATCH_NOT_FOUND));

        // 베팅 가능 여부 검증
        validateBettable(match.getStatus());

        // 유저 포인트 차감
        user.minusPoint(betAmount);

        // 배당률 계산
        BigDecimal odds = calculateOdds(match, request.selectedTeam());

        // 베팅 금액 저장
        updateTotalAmount(match, betAmount, request.selectedTeam());

        Bet bet = request.toEntity(user, match, odds);
        betRepository.save(bet);

        String selectedTeamName = selectedTeamName(match, request.selectedTeam());

        return BetCreateResponse.of(bet, selectedTeamName, user.getPoint());
    }

    @Transactional(readOnly = true)
    public Page<BetResponse> getBets(Long userId, Pageable pageable) {
        Page<Bet> bets = betRepository.findByUserId(userId, pageable);
        return bets.map(BetResponse::from);
    }

    @Transactional
    public BetDeleteResponse deleteBet(Long userId, Long betId) {
        Bet bet = betRepository.findByIdForDelete(betId)
                .orElseThrow(() -> new InvalidBetException(BetErrorCode.BET_NOT_FOUND));

        // 본인 베팅 확인
        if (!bet.getUser().getId().equals(userId)) {
            throw new InvalidBetException(BetErrorCode.PERMISSION_DENIED);
        }

        // 취소 가능 여부 확인
        Match match = matchRepository.findByIdForUpdate(bet.getMatch().getId())
                .orElseThrow(() -> new MatchException(MatchErrorCode.MATCH_NOT_FOUND));
        validateCancelable(match.getStatus());

        bet.delete();

        // 환불
        User user = bet.getUser();
        user.plusPoint(bet.getBetAmount());

        // 총 베팅 금액 되돌리기
        refundTotalAmount(match, bet.getBetAmount(), bet.getSelectedTeam());

        return BetDeleteResponse.of(bet, user);
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new InvalidUserException(UserErrorCode.USR_INVALID_USER_ID));
    }

    private void validateBettable(MatchStatus status) {
        if (!status.equals(MatchStatus.SCHEDULED)) {
            throw new InvalidBetException(BetErrorCode.MATCH_NOT_BETTABLE);
        }
    }

    private void validateCancelable(MatchStatus status) {
        if (!status.equals(MatchStatus.SCHEDULED)) {
            throw new InvalidBetException(BetErrorCode.MATCH_NOT_CANCELABLE);
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

    /***
     * 승 배당률 = 0.9 / 승 금액 비율
     * 0.9 = 1 - 0.1(수수료)
     * 승 금액 비율 = 승 총 베팅 금액 / 전체 베팅 금액(승 + 패)
     * 처음 배당률 지정 로직과
     * 이후 배당률 조정 로직이 필요하다. (이는 베팅 생성시 호출된다.)
     * 배당률 조정 로직 구현 시, 유저 비율도 고려하는 가중 조합형 배당률로 구현하는 것이 필요해 보인다.
     */
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
}
