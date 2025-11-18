package org.example.oddventure.domain.bet.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.oddventure.common.exception.CommonErrorCode;
import org.example.oddventure.domain.bet.dto.PointEventDto;
import org.example.oddventure.domain.bet.dto.request.BetCreateRequest;
import org.example.oddventure.domain.bet.dto.response.BetCreateResponse;
import org.example.oddventure.domain.bet.dto.response.BetDeleteResponse;
import org.example.oddventure.domain.bet.dto.response.BetResponse;
import org.example.oddventure.domain.bet.entity.Bet;
import org.example.oddventure.domain.bet.enums.SelectedTeam;
import org.example.oddventure.domain.bet.event.BetEventProducer;
import org.example.oddventure.domain.bet.exception.BetErrorCode;
import org.example.oddventure.domain.bet.exception.BetException;
import org.example.oddventure.domain.bet.repository.BetRepository;
import org.example.oddventure.domain.bet.service.BetTransactionService.CreateBetData;
import org.example.oddventure.domain.bet.service.BetTransactionService.DeleteBetData;
import org.example.oddventure.domain.match.event.MatchNotificationProducer;
import org.example.oddventure.domain.match.event.NotificationSubscriptionService;
import org.example.oddventure.domain.match.event.dto.MatchOddsUpdateDto;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BetService {

    private static final String USER_POINT_LOCK_PREFIX = "LOCK:USER_POINT:";
    private static final String MATCH_LOCK_PREFIX = "LOCK:MATCH:";
    private final BetRepository betRepository;
    private final BetEventProducer betEventProducer;
    private final RedissonClient redissonClient;
    private final BetTransactionService betTransactionService;
    private final MatchNotificationProducer matchNotificationProducer;
    private final NotificationSubscriptionService notificationSubscriptionService;

    public BetCreateResponse createBet(Long userId, BetCreateRequest request) {

        // 1. 락 2개 정의 (User, Match)
        String userLockKey = USER_POINT_LOCK_PREFIX + userId;
        String matchLockKey = MATCH_LOCK_PREFIX + request.matchId();
        RLock userLock = redissonClient.getLock(userLockKey);
        RLock matchLock = redissonClient.getLock(matchLockKey);

        // 2. MultiLock으로 두 락을 묶음
        RLock multiLock = redissonClient.getMultiLock(userLock, matchLock);

        try {
            // 3. 락 획득 시도 (10초 대기, 5초 점유)
            boolean isLocked = multiLock.tryLock(10, 5, TimeUnit.SECONDS);

            if (!isLocked) {
                log.warn("베팅 락 획득 실패. userId: {}, matchId: {}", userId, request.matchId());
                throw new BetException(BetErrorCode.BET_LOCK_FAILED);
            }

            // 4. 락 획득 성공 시, 분리된 트랜잭션 메서드 호출
            CreateBetData data = betTransactionService.createBetInternal(userId, request);

            // 5. 트랜잭션 커밋 성공 후, RabbitMQ 이벤트 발행
            notificationSubscriptionService.subscribeUserToMatch(userId, request.matchId());
            MatchOddsUpdateDto dto = new MatchOddsUpdateDto(data.bet().getMatch().getId(), data.selectedTeamName(),
                    data.odds());
            matchNotificationProducer.sendOddsChanged(dto);

            return BetCreateResponse.of(data.bet(), data.selectedTeamName(), data.userPointAfter());

        } catch (InterruptedException e) {
            log.error("베팅 락 대기 중 인터럽트 발생", e);
            Thread.currentThread().interrupt();
            throw new BetException(CommonErrorCode.INTERNAL_SERVER_ERROR);
        } finally {
            // 6. 락 해제
            multiLock.unlock();
        }
    }

    @Transactional(readOnly = true)
    public Page<BetResponse> getBets(Long userId, Pageable pageable) {
        Page<Bet> bets = betRepository.findByUserId(userId, pageable);
        return bets.map(BetResponse::from);
    }

    public BetDeleteResponse deleteBet(Long userId, Long betId) {

        // 락 획득 전 matchId를 알기 위해 Bet 정보 pre-fetch
        Bet preFetchedBet = betRepository.findById(betId)
                .orElseThrow(() -> new BetException(BetErrorCode.BET_NOT_FOUND));

        // 1. 락 2개 정의 (User, Match)
        String userLockKey = USER_POINT_LOCK_PREFIX + userId;
        String matchLockKey = MATCH_LOCK_PREFIX + preFetchedBet.getMatch().getId();
        RLock userLock = redissonClient.getLock(userLockKey);
        RLock matchLock = redissonClient.getLock(matchLockKey);

        RLock multiLock = redissonClient.getMultiLock(userLock, matchLock);

        try {
            // 2. 락 획득
            boolean isLocked = multiLock.tryLock(10, 5, TimeUnit.SECONDS);
            if (!isLocked) {
                log.warn("베팅 취소 락 획득 실패. userId: {}, betId: {}", userId, betId);
                throw new BetException(BetErrorCode.BET_LOCK_FAILED);
            }

            // 3. 락 획득 후 트랜잭션 호출
            DeleteBetData data = betTransactionService.deleteBetInternal(userId, betId);

            return BetDeleteResponse.of(data.bet(), data.user());

        } catch (InterruptedException e) {
            log.error("베팅 취소 락 대기 중 인터럽트 발생", e);
            Thread.currentThread().interrupt();
            throw new BetException(CommonErrorCode.INTERNAL_SERVER_ERROR);
        } finally {
            // 4. 락 해제
            multiLock.unlock();
        }
    }

    @Transactional(readOnly = true)
    public List<Bet> findByMatchId(Long matchId) {
        return betRepository.findByMatchId(matchId);
    }

    @Transactional
    public void settleBet(Bet bet, SelectedTeam winner) {
        if (bet.getSelectedTeam().equals(winner) && !bet.isDeleted()) {
            bet.setWin(true);
            BigDecimal point = bet.getBetAmount().multiply(bet.getOddsAtBetting());
            betEventProducer.producePointEvent(PointEventDto.from(bet.getUser().getId(), point));
        }
    }
}