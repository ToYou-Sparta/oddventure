package org.example.oddventure.domain.user.unit;

import static org.assertj.core.api.Assertions.assertThat;
import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.example.oddventure.base.RedisTestContainerConfig;
import org.example.oddventure.domain.admin.dto.request.PointAdjustRequest;
import org.example.oddventure.domain.user.entity.User;
import org.example.oddventure.domain.user.repository.UserRepository;
import org.example.oddventure.domain.user.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
public class UserServiceConcurrencyTest extends RedisTestContainerConfig {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    /**
     * 테스트 실행 전, 동시성 테스트에 사용할 사용자를 생성하고
     * 초기 포인트를 0으로 설정
     */
    @BeforeEach
    void setUp() {
        User user = User.builder()
                .username("concurrentUser")
                .email("concurrent@test.com")
                .password("password")
                .build();

        // 테스트 전 초기 포인트 0으로 설정
        // (User 생성자는 기본 1000 포인트를 가지므로 1000을 차감)
        user.minusPoint(new BigDecimal("1000"));
        testUser = userRepository.saveAndFlush(user);

        assertThat(testUser.getPoint()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    /**
     * 테스트 실행 후, 생성된 사용자를 삭제
     */
    @AfterEach
    void tearDown() {
        userRepository.deleteById(testUser.getId());
    }

    /**
     * 100개의 스레드가 동시에 10포인트씩 지급을 요청할 때,
     * Lost Update 없이 총 1000 포인트가 정확히 적립되는지 테스트
     */
    @Test
    @DisplayName("동시에 100명이 10포인트씩 지급 요청 시 1000포인트가 정확히 적립된다 (분산 락)")
    void adjustUserPoints_ConcurrencyTest_Success() throws InterruptedException {
        // given
        int threadCount = 100;
        BigDecimal pointToAdd = BigDecimal.TEN;
        BigDecimal expectedResult = pointToAdd.multiply(BigDecimal.valueOf(threadCount));
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);
        PointAdjustRequest request = new PointAdjustRequest(pointToAdd, "동시성 테스트");

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    userService.adjustUserPoints(testUser.getId(), request);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // then
        User updatedUser = userRepository.findById(testUser.getId()).orElseThrow();

        assertThat(updatedUser.getPoint()).isEqualByComparingTo(expectedResult);
    }
}