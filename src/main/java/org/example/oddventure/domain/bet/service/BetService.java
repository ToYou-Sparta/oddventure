package org.example.oddventure.domain.bet.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.concurrent.TimeUnit;
import org.example.oddventure.common.exception.CommonErrorCode;
import org.example.oddventure.domain.bet.dto.request.BetCreateRequest;
import org.example.oddventure.domain.bet.dto.response.BetCreateResponse;
import org.example.oddventure.domain.bet.dto.response.BetDeleteResponse;
import org.example.oddventure.domain.bet.dto.response.BetResponse;
import org.example.oddventure.domain.bet.entity.Bet;
import org.example.oddventure.domain.bet.exception.BetErrorCode;
import org.example.oddventure.domain.bet.exception.BetException;
import org.example.oddventure.domain.bet.repository.BetRepository;
import org.example.oddventure.domain.bet.service.BetTransactionService.CreateBetData;
import org.example.oddventure.domain.bet.service.BetTransactionService.DeleteBetData;
import org.example.oddventure.domain.event.RedisPublisher;
import org.example.oddventure.domain.match.dto.event.MatchOddsUpdateDto;
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

    private final BetRepository betRepository;
    private final RedisPublisher redisPublisher;
    private final RedissonClient redissonClient;
    private final BetTransactionService betTransactionService;
    private static final String USER_POINT_LOCK_PREFIX = "LOCK:USER_POINT:";
    private static final String MATCH_LOCK_PREFIX = "LOCK:MATCH:";

    public BetCreateResponse createBet(Long userId, BetCreateRequest request) {

        // 1. 락 2개 정의 (User, Match)
        String userLockKey = USER_POINT_LOCK_PREFIX + userId;
        String matchLockKey = MATCH_LOCK_PREFIX + request.matchId();

        RLock userLock = redissonClient.getLock(userLockKey);
        RLock matchLock = redissonClient.getLock(matchLockKey);

        // 2. MultiLock으로 두 락을 묶음
        RLock multiLock = redissonClient.getMultiLock(userLock, matchLock);

        try {
            // 3. 락 획득 시도 (데드락 방지를 위해 순서대로 락을 잡음)
            boolean isLocked = multiLock.tryLock(10, 5, TimeUnit.SECONDS);

            if (!isLocked) {
                log.warn("베팅 락 획득 실패. userId: {}, matchId: {}", userId, request.matchId());
                throw new BetException(BetErrorCode.BET_LOCK_FAILED);
            }

            // 4. 락 획득 성공 시, 분리된 트랜잭션 메서드 호출
            CreateBetData data = betTransactionService.createBetInternal(userId, request);

            // 5. 트랜잭션 커밋 성공 후, Redis 이벤트 발행
            MatchOddsUpdateDto dto = new MatchOddsUpdateDto(data.bet().getMatch().getId(), data.selectedTeamName(), data.odds());
            redisPublisher.publish("match:" + dto.matchId() + ":odds", dto);

            return BetCreateResponse.of(data.bet(), data.selectedTeamName(), data.userPointAfter());

        } catch (InterruptedException e) {
            log.error("베팅 락 대기 중 인터럽트 발생", e);
            Thread.currentThread().interrupt();
            throw new BetException(CommonErrorCode.INTERNAL_SERVER_ERROR);
        } finally {
            // 6. 락 해제 (userLock, matchLock 모두 해제됨)
            multiLock.unlock();
        }
    }

    @Transactional(readOnly = true)
    public Page<BetResponse> getBets(Long userId, Pageable pageable) {
        Page<Bet> bets = betRepository.findByUserId(userId, pageable);
        return bets.map(BetResponse::from);
    }

    public BetDeleteResponse deleteBet(Long userId, Long betId) {

        // 베팅 취소는 락 대상이 많으므로 락 획득 전 Bet 정보가 필요
        // 단, 이 조회는 락을 잡지 않으므로 데이터가 정확하지 않을 수 있음
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
            // (트랜잭션 내부에서 데이터를 다시 조회하므로 정합성 보장)
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
}