package org.example.oddventure.domain.match.unit;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.example.oddventure.base.RedisTestContainerConfig;
import org.example.oddventure.domain.match.entity.Match;
import org.example.oddventure.domain.match.repository.MatchRepository;
import org.example.oddventure.domain.match.service.MatchService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.util.StopWatch;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {"spring.data.redis.host=localhost", "spring.data.redis.port=0"})
public class MatchServiceCacheTest extends RedisTestContainerConfig {

    @Autowired
    private MatchService matchService;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private Match finishedMatch;

    @BeforeEach
    void setUp() {
        redisTemplate.getConnectionFactory().getConnection().flushAll();

        // 1. 'FINISHED' 상태의 매치 생성
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
    }

    @Test
    @DisplayName("종료된 경기 상세 조회 캐싱 적용 전/후 성능 비교 (StopWatch)")
    void testFinishedMatchCachePerformance() {
        // given
        Long finishedMatchId = finishedMatch.getId();
        StopWatch stopWatch = new StopWatch("FinishedMatch Cache Performance");

        // when
        // 1. "캐싱 적용 전" (Cache Miss)
        stopWatch.start("Cache Miss (DB)");
        matchService.getMatch(finishedMatchId);
        stopWatch.stop();

        // when
        // 2. "캐싱 적용 후" (Cache Hit)
        stopWatch.start("Cache Hit (Redis)");
        matchService.getMatch(finishedMatchId);
        stopWatch.stop();

        // then
        // 3. 결과 출력
        System.out.println(stopWatch.prettyPrint());
        long missTime = stopWatch.getTaskInfo()[0].getTimeMillis();
        long hitTime = stopWatch.getTaskInfo()[1].getTimeMillis();

        // 4. 검증
        assertThat(hitTime).isLessThan(missTime);
    }
}