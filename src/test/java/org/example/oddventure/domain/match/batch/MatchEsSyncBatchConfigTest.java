package org.example.oddventure.domain.match.batch;

import org.example.oddventure.domain.match.document.MatchDocument;
import org.example.oddventure.domain.match.entity.Match;
import org.example.oddventure.domain.match.repository.MatchRepository;
import org.example.oddventure.domain.match.repository.elasticsearch.MatchSearchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * MatchEsSyncBatchConfig 통합 테스트
 *
 * <p>Spring Batch를 사용한 MySQL → Elasticsearch 동기화 테스트
 */
@SpringBatchTest
@SpringBootTest(properties = "spring.rabbitmq.listener.simple.auto-startup=false")
class MatchEsSyncBatchConfigTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private MatchSearchRepository matchSearchRepository;

    @Autowired
    private Job matchEsSyncJob;

    @BeforeEach
    void setUp() {
        jobLauncherTestUtils.setJob(matchEsSyncJob);

        // 테스트 데이터 초기화
        matchSearchRepository.deleteAll();
        matchRepository.deleteAll();
    }

    @Test
    @DisplayName("MySQL → Elasticsearch 전체 동기화 성공")
    void matchEsSyncJob_success() throws Exception {
        // given: MySQL에 Match 데이터 저장
        List<Match> matches = createTestMatches(100);
        matchRepository.saveAll(matches);

        // when: Batch Job 실행
        JobParameters params = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();

        JobExecution execution = jobLauncherTestUtils.launchJob(params);

        // then: Job 실행 성공 확인
        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        // Elasticsearch에 데이터가 동기화되었는지 확인
        long esCount = matchSearchRepository.count();
        assertThat(esCount).isEqualTo(100);

        // 첫 번째 Match 데이터 확인
        Match firstMatch = matches.get(0);
        MatchDocument document = matchSearchRepository.findById(String.valueOf(firstMatch.getId()))
                .orElse(null);

        assertThat(document).isNotNull();
        assertThat(document.getMatchName()).isEqualTo(firstMatch.getMatchName());
        assertThat(document.getTeamA()).isEqualTo(firstMatch.getTeamA());
        assertThat(document.getTeamB()).isEqualTo(firstMatch.getTeamB());
    }

    /**
     * 테스트용 Match 데이터 생성
     */
    private List<Match> createTestMatches(int count) {
        List<Match> matches = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Match match = Match.builder()
                    .fetchId((long) i)
                    .matchName("Test Match " + i)
                    .teamA("Team A " + i)
                    .teamB("Team B " + i)
                    .startTime(LocalDateTime.now().plusDays(i))
                    .build();
            matches.add(match);
        }
        return matches;
    }
}