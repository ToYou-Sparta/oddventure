package org.example.oddventure.domain.match.scheduler;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.oddventure.domain.grid.dto.MatchResultDto;
import org.example.oddventure.domain.grid.service.GridService;
import org.example.oddventure.domain.match.entity.Match;
import org.example.oddventure.domain.match.enums.MatchStatus;
import org.example.oddventure.domain.match.repository.MatchRepository;
import org.example.oddventure.domain.match.service.MatchService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MatchScheduler {

    private final MatchRepository matchRepository;
    private final MatchService matchService;
    private final GridService gridService;

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
        } else {
            log.info("끝나지 않은 경기입니다. matchId={}, fetchId={}", match.getId(), match.getFetchId());
        }
    }
}
