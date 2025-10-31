package org.example.oddventure.domain.bet.unit;

import static org.assertj.core.api.Assertions.assertThat;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.example.oddventure.domain.bet.dto.request.BetCreateRequest;
import org.example.oddventure.domain.bet.enums.SelectedTeam;
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

@SpringBootTest
public class BetServiceConcurrencyTest {

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
     * 1000 포인트를 가진 사용자가 100 포인트 베팅을 20회 동시 시도할 때,
     * 정확히 10회만 성공하고 10회는 "포인트 부족" 예외가 발생하며,
     * 사용자의 최종 잔액은 0원이 되는지 테스트함
     */
    @Test
    @DisplayName("1000포인트 유저가 100포인트 베팅 20회 동시 시도 시, 10회만 성공하고 잔액은 0이 된다")
    void createBet_ConcurrencyTest_SuccessAndFailure() throws InterruptedException {
        // given
        int threadCount = 20;
        long betAmount = 100L; // 100 포인트 베팅

        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // 동시성 환경에서 성공/실패 카운트를 위한 Atomic 변수
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
                    if (e.getErrorCode().getMessage().contains("부족")) {
                        failCount.incrementAndGet(); // "보유 포인트가 부족합니다." 예외 시 실패
                    }
                } catch (Exception e) {
                    // 디버깅을 위해 다른 예외도 출력
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
        // DB에서 최종 사용자 정보 및 베팅 내역 조회
        User updatedUser = userRepository.findById(testUser.getId()).orElseThrow();
        long betCount = betRepository.count();

        // 10번은 성공
        assertThat(successCount.get()).isEqualTo(10);
        // 10번은 포인트 부족으로 실패
        assertThat(failCount.get()).isEqualTo(10);
        // DB에 저장된 Bet 엔티티도 10개
        assertThat(betCount).isEqualTo(10);
        // 유저의 최종 잔액은 0 (1000 - 100 * 10)
        assertThat(updatedUser.getPoint()).isEqualByComparingTo(BigDecimal.ZERO);
    }
}