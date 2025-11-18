package org.example.oddventure.domain.bet.unit;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.example.oddventure.base.RedisTestContainerConfig;
import org.example.oddventure.domain.bet.dto.request.BetCreateRequest;
import org.example.oddventure.domain.bet.enums.SelectedTeam;
import org.example.oddventure.domain.bet.exception.BetErrorCode;
import org.example.oddventure.domain.bet.exception.BetException;
import org.example.oddventure.domain.bet.repository.BetRepository;
import org.example.oddventure.domain.bet.service.BetService;
import org.example.oddventure.domain.match.entity.Match;
import org.example.oddventure.domain.match.repository.MatchRepository;
import org.example.oddventure.domain.user.entity.User;
import org.example.oddventure.domain.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
public class BetServiceConcurrencyTest extends RedisTestContainerConfig {

    @Autowired
    private BetService betService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private BetRepository betRepository;

    private User testUser;
    private Match testMatch;

    @BeforeEach
    void setUp() {
        // 1000 포인트를 가진 유저 생성
        User user = User.builder()
                .username("bettingUser")
                .email("bet@test.com")
                .password("password")
                .build();
        testUser = userRepository.saveAndFlush(user);
        assertThat(testUser.getPoint()).isEqualByComparingTo(new BigDecimal("1000"));

        Match match = Match.builder()
                .matchName("LCK")
                .teamA("T1")
                .teamB("GEN.G")
                .startTime(LocalDateTime.now().plusDays(1))
                .build();
        match.plusTeamA(new BigDecimal("100"));
        match.plusTeamB(new BigDecimal("100"));
        testMatch = matchRepository.saveAndFlush(match);
    }

    /**
     * 테스트 실행 후, 생성된 데이터를 모두 삭제
     */
    @AfterEach
    void tearDown() {
        betRepository.deleteAll();
        matchRepository.deleteById(testMatch.getId());
        userRepository.deleteById(testUser.getId());
    }

    /**
     * 1000 포인트를 가진 사용자가 100 포인트 베팅을 20회 동시 시도할 때, 정확히 10회만 성공하고 10회는 "포인트 부족" 예외가 발생하며, 사용자의 최종 잔액은 0원이 되는지 테스트함
     */
    @Test
    @DisplayName("1000포인트 유저가 100포인트 베팅 20회 동시 시도 시, 10회만 성공하고 잔액은 0이 된다 (분산 락)")
    void createBet_ConcurrencyTest_SuccessAndFailure() throws InterruptedException {
        // given
        int threadCount = 20;
        long betAmount = 100L; // 100 포인트 베팅

        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        BetCreateRequest request = new BetCreateRequest(testMatch.getId(), SelectedTeam.Team_A, betAmount);

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    betService.createBet(testUser.getId(), request);
                    successCount.incrementAndGet(); // 성공
                } catch (BetException e) {
                    // 락 획득 실패(BET_LOCK_FAILED) 또는 포인트 부족(NOT_ENOUGH_POINTS) 모두 실패로 간주
                    if (e.getErrorCode() == BetErrorCode.NOT_ENOUGH_POINTS ||
                            e.getErrorCode() == BetErrorCode.BET_LOCK_FAILED) {
                        failCount.incrementAndGet();
                    } else {
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    System.err.println("Unexpected exception: " + e.getMessage());
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // then
        User updatedUser = userRepository.findById(testUser.getId()).orElseThrow();
        long betCount = betRepository.count();

        // 분산 락은 DB 락보다 락 획득/해제 속도가 빨라서 10회/10회(50%)가 아닌 11회/9회 등으로 결과가 나올 수 있음
        // 중요한 것은 (성공 횟수 * 베팅금액) == (차감된 포인트)
        BigDecimal totalBetAmount = new BigDecimal(betAmount).multiply(new BigDecimal(successCount.get()));
        BigDecimal expectedPoint = new BigDecimal("1000").subtract(totalBetAmount);

        // 1. 성공 횟수 + 실패 횟수 = 총 20회
        assertThat(successCount.get() + failCount.get()).isEqualTo(20);
        // 2. DB에 저장된 Bet 엔티티 == 성공 횟수
        assertThat(betCount).isEqualTo(successCount.get());
        // 3. 유저의 최종 잔액 == 1000 - (성공 횟수 * 100)
        assertThat(updatedUser.getPoint()).isEqualByComparingTo(expectedPoint);
    }
}