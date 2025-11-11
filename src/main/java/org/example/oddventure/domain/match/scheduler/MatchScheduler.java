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
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
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
    private final RedisTemplate<String, Object> redisTemplate;

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

    // 5분마다 Redis의 조회수를 RDB로 동기화
    @Scheduled(cron = "0 */5 * * * *") // 5분마다 실행
    public void syncViewCountsToDB() {
        log.info("Redis 조회수 DB 동기화 시작");
        String pattern = "match:viewcount:*";
        ScanOptions options = ScanOptions.scanOptions().match(pattern).count(100).build();
        Cursor<String> cursor = redisTemplate.scan(options);

        while (cursor.hasNext()) {
            String key = cursor.next();
            try {
                // 1. Redis에서 현재 조회수 가져오기
                Object rawValue = redisTemplate.opsForValue().get(key);
                if (rawValue == null) continue;

                Long viewCount = Long.parseLong(String.valueOf(rawValue));

                // 2. Key에서 matchId 파싱 (예: "match:viewcount:1" -> "1")
                Long matchId = Long.parseLong(key.substring(key.lastIndexOf(':') + 1));

                // 3. MatchService의 @Transactional 메서드를 호출하여 DB 업데이트
                matchService.updateViewCount(matchId, viewCount);

            } catch (Exception e) {
                log.error("키 처리 중 오류 발생: {} (key: {})", e.getMessage(), key);
            }
        }
        log.info("Redis 조회수 DB 동기화 완료.");
    }
}
