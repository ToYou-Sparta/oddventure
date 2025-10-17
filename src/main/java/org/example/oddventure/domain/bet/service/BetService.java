package org.example.oddventure.domain.bet.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.example.oddventure.domain.bet.dto.request.BetCreateRequest;
import org.example.oddventure.domain.bet.dto.response.BetCreateResponse;
import org.example.oddventure.domain.bet.entity.Bet;
import org.example.oddventure.domain.bet.enums.SelectedTeam;
import org.example.oddventure.domain.bet.exception.BetErrorCode;
import org.example.oddventure.domain.bet.exception.InvalidBetException;
import org.example.oddventure.domain.bet.repository.BetRepository;
import org.example.oddventure.domain.match.entity.Match;
import org.example.oddventure.domain.match.exception.MatchErrorCode;
import org.example.oddventure.domain.match.exception.MatchException;
import org.example.oddventure.domain.match.repository.MatchRepository;
import org.example.oddventure.domain.user.entity.User;
import org.example.oddventure.domain.user.exception.InvalidUserException;
import org.example.oddventure.domain.user.exception.UserErrorCode;
import org.example.oddventure.domain.user.repository.UserRepository;
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
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new InvalidUserException(UserErrorCode.USR_INVALID_USER_ID));

        // 잔액 부족
        if (user.getPoint().compareTo(request.betAmount()) < 0) {
            throw new InvalidBetException(BetErrorCode.NOT_ENOUGH_POINTS);
        }

        Match match = matchRepository.findById(request.matchId())
                .orElseThrow(() -> new MatchException(MatchErrorCode.MATCH_NOT_FOUND));

        // 베팅 가능 여부 검증
        validateBettable(match.getStartTime());

        // 유저 포인트 차감
        user.minusPoint(request.betAmount());

        // 베팅 금액 저장
        updateTotalAmount(match, request);

        // 배당률 계산
        BigDecimal odds = calculateOdds(match, request);

        Bet bet = request.toEntity(user, match, odds);

        return BetCreateResponse.of(bet, user.getPoint());
    }

    private void validateBettable(LocalDateTime matchStartTime) {
        if (matchStartTime.isBefore(LocalDateTime.now())) {
            throw new InvalidBetException(BetErrorCode.MATCH_NOT_BETTABLE);
        }
    }

    private void updateTotalAmount(Match match, BetCreateRequest request) {
        if (request.selectedTeam() == SelectedTeam.Team_A) {
            match.plusTeamA(request.betAmount());
        } else if (request.selectedTeam() == SelectedTeam.Team_B) {
            match.plusTeamB(request.betAmount());
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
    private BigDecimal calculateOdds(Match match, BetCreateRequest request) {
        BigDecimal total = match.getTotalAmountA().add(match.getTotalAmountB());
        BigDecimal probability;

        if (request.selectedTeam().equals(SelectedTeam.Team_A)) {
            probability = match.getTotalAmountA().divide(total, 2, RoundingMode.HALF_UP);
        } else {
            probability = match.getTotalAmountB().divide(total, 2, RoundingMode.HALF_UP);
        }

        return new BigDecimal("0.9").divide(probability, 2, RoundingMode.HALF_UP);
    }
}
