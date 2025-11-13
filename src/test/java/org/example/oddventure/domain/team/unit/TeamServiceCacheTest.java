package org.example.oddventure.domain.team.unit;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.example.oddventure.base.RedisTestContainerConfig;
import org.example.oddventure.domain.team.entity.Team;
import org.example.oddventure.domain.team.repository.TeamRepository;
import org.example.oddventure.domain.team.service.TeamService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.StopWatch;

@SpringBootTest
@ActiveProfiles("test")
class TeamServiceCacheTest extends RedisTestContainerConfig {

    @Autowired
    private TeamService teamService;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @BeforeEach
    void setUp() {
        // 캐시 비우기
        redisTemplate.getConnectionFactory().getConnection().flushAll();

        // 테스트 데이터 1개 삽입
        if (teamRepository.count() == 0) {
            teamRepository.save(Team.builder().name("T1").build());
        }
    }

    @Test
    @DisplayName("팀 상세 조회 캐싱 적용 전/후 성능 비교")
    void testTeamDetailCachePerformance() {

        Long teamId = 1L;
        StopWatch stopWatch = new StopWatch();

        // 1. "캐싱 적용 전" 측정 (캐시 MISS)
        stopWatch.start("Cache Miss");
        teamService.getTeamById(teamId); // 처음 호출 (DB 조회 + 캐시 저장)
        stopWatch.stop();

        // 2. "캐싱 적용 후" 측정 (캐시 HIT)
        stopWatch.start("Cache Hit");
        teamService.getTeamById(teamId); // 두 번째 호출 (Redis에서 조회)
        stopWatch.stop();

        // 3. 결과 출력
        System.out.println(stopWatch.prettyPrint());
        long firstCallTime = stopWatch.getTaskInfo()[0].getTimeMillis();
        long secondCallTime = stopWatch.getTaskInfo()[1].getTimeMillis();

        // 4. 검증
        assertThat(firstCallTime).isGreaterThan(secondCallTime);
    }
}
