package org.example.oddventure.domain.match.scheduler;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.oddventure.domain.bet.entity.Bet;
import org.example.oddventure.domain.bet.enums.SelectedTeam;
import org.example.oddventure.domain.bet.service.BetService;
import org.example.oddventure.domain.grid.dto.MatchResultDto;
import org.example.oddventure.domain.grid.service.GridService;
import org.example.oddventure.domain.match.entity.Match;
import org.example.oddventure.domain.match.enums.MatchStatus;
import org.example.oddventure.domain.match.repository.MatchRepository;
import org.example.oddventure.domain.match.service.MatchService;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MatchScheduler {

    private final MatchRepository matchRepository;
    private final MatchService matchService;
    private final GridService gridService;
    private final BetService betService;
    private final JobLauncher jobLauncher;
    private final Job matchScheduleJob;

    @Scheduled(cron = "0 0 4 * * *")
    public void runMatchSyncJob() throws Exception {
        JobParameters params = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();

        jobLauncher.run(matchScheduleJob, params);
    }

    @Scheduled(cron = "0 0 13 * * *", zone = "Asia/Seoul")
    public void autoFinishMatches() {
        List<Match> matches = matchRepository.findByMatchStatus(MatchStatus.ONGOING, LocalDateTime.now().minusDays(2));

        matches.forEach(match -> {
            if (match.isDeleted()) {
                log.info("삭제된 매치입니다. matchId={}, fetchId={}", match.getId(), match.getFetchId());
                return;
            }

            MatchResultDto result = gridService.fetchMatchResult(match.getFetchId());
            handleResult(match, result);
        });
    }

    private void handleResult(Match match, MatchResultDto result) {
        if (result.finished()) {
            matchService.updateMatchResult(
                    match.getFetchId(),
                    result.winner(),
                    result.loser()
            );
            /*
              매치에 연관된 베팅 다 조회
              이겼는지 업데이트
              이긴 것들만 포인트 지급 이벤트 발행
             */
            SelectedTeam winnerTeam = result.winner().equals(match.getTeamA())
                    ? SelectedTeam.Team_A
                    : SelectedTeam.Team_B;

            List<Bet> bets = betService.findByMatchId(match.getId());
            for (Bet bet : bets) {
                betService.settleBet(bet, winnerTeam);
            }
        } else {
            log.info("끝나지 않은 경기입니다. matchId={}, fetchId={}", match.getId(), match.getFetchId());
        }
    }
}
