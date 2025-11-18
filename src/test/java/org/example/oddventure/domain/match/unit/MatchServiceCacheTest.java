package org.example.oddventure.domain.match.unit;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.example.oddventure.domain.match.entity.Match;
import org.example.oddventure.domain.match.repository.MatchRepository;
import org.example.oddventure.domain.match.service.MatchService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.StopWatch;

@SpringBootTest
@ActiveProfiles("test")
public class MatchServiceCacheTest {

    @Autowired
    private MatchService matchService;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private CacheManager cacheManager;

    private Match finishedMatch;

    @BeforeEach
    void setUp() {
        try {
            var connection = redisTemplate.getConnectionFactory().getConnection();
            if (connection != null) {
                connection.flushAll();
                connection.close();
            }
        } catch (Exception e) {
            System.err.println("Redis flush 실패: " + e.getMessage());
        }

        // 'FINISHED' 상태의 매치 생성
        Match match1 = Match.builder()
                .matchName("Finished Match")
                .teamA("T1")
                .teamB("GEN.G")
                .startTime(LocalDateTime.now().minusDays(1))
                .build();

        match1.finishMatch("T1", "GEN.G");
        finishedMatch = matchRepository.save(match1);
    }

    @AfterEach
    void tearDown() {
        matchRepository.deleteAll();

        try {
            var connection = redisTemplate.getConnectionFactory().getConnection();
            if (connection != null) {
                connection.flushAll();
                connection.close();
            }
        } catch (Exception e) {
            System.err.println("Redis flush 실패: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("종료된 경기 상세 조회 캐싱 성능 비교")
    void testFinishedMatchCachePerformance() {
        Long finishedMatchId = finishedMatch.getId();
        StopWatch stopWatch = new StopWatch("FinishedMatch Cache Performance");

        // 1. 캐시 비우기 (명시적 캐시 클리어)
        var matchCache = cacheManager.getCache("match");
        if (matchCache != null) {
            matchCache.clear();
        }

        // 2. Cache Miss (DB 조회) - 첫 번째 호출
        stopWatch.start("Cache Miss (DB)");
        var dbResult = matchService.getMatch(finishedMatchId);
        stopWatch.stop();
        assertThat(dbResult).isNotNull();

        // 3. Cache Hit (Redis 조회) - 여러 번 반복
        long totalHitTime = 0;
        for (int i = 0; i < 5; i++) {
            stopWatch.start("Cache Hit #" + (i + 1));
            var cachedResult = matchService.getMatch(finishedMatchId);
            stopWatch.stop();

            long taskTime = stopWatch.getTaskInfo()[i + 1].getTimeMillis();
            totalHitTime += taskTime;
            assertThat(cachedResult).isNotNull();
        }

        // 4. 결과 분석 및 출력
        System.out.println("\n" + "=".repeat(60));
        System.out.println(stopWatch.prettyPrint());
        System.out.println("=".repeat(60));

        long missTime = stopWatch.getTaskInfo()[0].getTimeMillis();
        long avgHitTime = totalHitTime / 5;
        double improvementRate = ((double)(missTime - avgHitTime) / missTime) * 100;

        // 5. 성능 검증
        assertThat(avgHitTime).isLessThan(missTime);

        // 6. 성능 결과 출력
        System.out.printf("%n성능 개선 결과:%n");
        System.out.printf("   Cache Miss (DB):     %d ms%n", missTime);
        System.out.printf("   Avg Cache Hit:       %d ms%n", avgHitTime);
        System.out.printf("   개선율:              %.1f%%%n", improvementRate);
        System.out.printf("   응답속도 향상:       %.1f배%n%n", (double) missTime / avgHitTime);
    }
}