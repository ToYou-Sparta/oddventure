package org.example.oddventure.domain.match.batch;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.example.oddventure.domain.bet.entity.Bet;
import org.example.oddventure.domain.bet.enums.SelectedTeam;
import org.example.oddventure.domain.bet.repository.BetRepository;
import org.example.oddventure.domain.match.entity.Match;
import org.example.oddventure.domain.match.repository.MatchRepository;
import org.example.oddventure.domain.user.entity.User;
import org.example.oddventure.domain.user.repository.UserRepository;
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

@SpringBatchTest
@SpringBootTest(properties = "spring.rabbitmq.listener.simple.auto-startup=false")
class PointJobConfigTest {

    User user1;
    User user2;
    Match match;
    Bet winningBet;
    Bet losingBet;
    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MatchRepository matchRepository;
    @Autowired
    private BetRepository betRepository;

    @Autowired
    private Job pointSetJob;

    @BeforeEach
    void setUp() {
        jobLauncherTestUtils.setJob(pointSetJob);

        // 유저 생성
        user1 = User.builder()
                .username("test1")
                .email("test12341@test.com")
                .password("test1234!")
                .build();

        user2 = User.builder()
                .username("test2")
                .email("test12342@test.com")
                .password("test1234!")
                .build();

        userRepository.save(user1);
        userRepository.save(user2);

        Long fetchId = 1L;
        match = Match.builder()
                .fetchId(fetchId)
                .matchName("T1 vs GEN.G")
                .teamA("T1")
                .teamB("GEN.G")
                .startTime(LocalDateTime.now().plusDays(1))
                .build();
        match.finishMatch("T1", "GEN.G");

        matchRepository.save(match);

        // 이긴 베팅
        winningBet = Bet.builder()
                .user(user1)
                .match(match)
                .selectedTeam(SelectedTeam.Team_A)
                .betAmount(new BigDecimal("1000"))
                .oddsAtBetting(new BigDecimal("2"))
                .build();

        // 진 베팅
        losingBet = Bet.builder()
                .user(user2)
                .match(match)
                .selectedTeam(SelectedTeam.Team_B)
                .betAmount(new BigDecimal("1000"))
                .oddsAtBetting(new BigDecimal("2"))
                .build();

        betRepository.save(winningBet);
        betRepository.save(losingBet);
    }

    @Test
    @DisplayName("정산 배치 포인트 지급 성공")
    void pointSetJob_success() throws Exception {

        // given
        JobParameters params = new JobParametersBuilder()
                .addString("matchIds", match.getId().toString())
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();

        // when
        JobExecution execution = jobLauncherTestUtils.launchJob(params);

        // then
        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        User updatedUser1 = userRepository.findById(user1.getId()).orElseThrow();
        User updatedUser2 = userRepository.findById(user2.getId()).orElseThrow();
        // 이긴 베팅 → 1000 * 2 = 2000 증가
        assertThat(updatedUser1.getPoint()).isEqualByComparingTo(BigDecimal.valueOf(3000));
        // 진 베팅 → 변화 없음
        assertThat(updatedUser2.getPoint()).isEqualByComparingTo(BigDecimal.valueOf(1000));
    }
}