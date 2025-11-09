package org.example.oddventure.domain.match.scheduler;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.oddventure.domain.bet.exception.BetErrorCode;
import org.example.oddventure.domain.bet.exception.BetException;
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
    private final JobLauncher jobLauncher;
    private final Job pointSetJob;

    @Scheduled(cron = "0 0 13 * * *", zone = "Asia/Seoul")
    public void autoFinishMatches() {
        List<Match> matches = matchRepository.findByMatchStatus(MatchStatus.ONGOING, LocalDateTime.now().minusDays(2));
        List<Long> matchIds = new ArrayList<>();

        matches.forEach(match -> {
            if (match.isDeleted()) {
                log.info("삭제된 매치입니다. matchId={}, fetchId={}", match.getId(), match.getFetchId());
                return;
            }

            MatchResultDto result = gridService.fetchMatchResult(match.getFetchId());

            if (!result.finished()) {
                log.info("끝나지 않은 경기입니다. matchId={}, fetchId={}", match.getId(), match.getFetchId());
                return;
            }

            matchService.updateMatchResult(match.getFetchId(), result.winner(), result.loser());

            matchIds.add(match.getId());
        });

        if (matchIds.isEmpty()) {
            return;
        }

        JobParameters jobParameters = new JobParametersBuilder()
                .addString("matchIds", matchIds.stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining(",")))
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();

        try {
            jobLauncher.run(pointSetJob, jobParameters);
        } catch (Exception e) {
            throw new BetException(BetErrorCode.POINT_BATCH_EXCEPTION);
        }
    }
}
