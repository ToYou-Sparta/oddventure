package org.example.oddventure.support.fixture.match;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.oddventure.domain.match.entity.Match;
import org.example.oddventure.domain.match.event.MatchEsSyncPublisher;
import org.example.oddventure.domain.match.repository.MatchRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 매치 테스트 데이터 생성 서비스
 * 성능 테스트 및 개발 환경에서 대량의 더미 데이터를 생성하기 위한 용도
 */
@Profile({"local","dev"})
@Slf4j
@Service
@RequiredArgsConstructor
public class MatchFixtureService {

    private final MatchRepository matchRepository;
    private final MatchEsSyncPublisher matchEsSyncPublisher;

    /**
     * 성능 테스트용 대량 더미 데이터 생성
     * 개발 환경에서만 사용
     *
     * @param count 생성할 매치 수
     */
    @Transactional
    public void generateTestData(int count) {
        Random random = new Random();
        List<Match> matches = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            // 1. 팀 A를 랜덤 선택
            String teamA = MatchFixtureData.TEAMS[random.nextInt(MatchFixtureData.TEAMS.length)];

            // 2. 팀 B를 선택 (단, 팀 A와 다르게)
            String teamB;
            do {
                teamB = MatchFixtureData.TEAMS[random.nextInt(MatchFixtureData.TEAMS.length)];
            } while (teamA.equals(teamB));

            // 3. 경기명 생성
            String matchName = MatchFixtureData.MATCH_NAMES[random.nextInt(MatchFixtureData.MATCH_NAMES.length)];

            // 4. 경기 시작 시간
            LocalDateTime startTime = LocalDateTime.now()
                    .plusDays(random.nextInt(30))
                    .withHour(random.nextInt(24))
                    .withMinute(0)
                    .withSecond(0);

            // 5. 매치 생성
            Match match = Match.builder()
                    .matchName(matchName)
                    .teamA(teamA)
                    .teamB(teamB)
                    .startTime(startTime)
                    .build();

            matches.add(match);

            // 6. 배치 저장 (BATCH_SIZE씩 끊어서)
            if (matches.size() >= MatchFixtureData.BATCH_SIZE) {
                List<Match> savedMatches = matchRepository.saveAll(matches);
                publishEsSyncEvents(savedMatches);
                matches.clear();
                log.info("배치 저장 : {} / {}", i + 1, count);
            }
        }

        // 7. 남은 데이터 저장
        if (!matches.isEmpty()) {
            List<Match> savedMatches = matchRepository.saveAll(matches);
            publishEsSyncEvents(savedMatches);
        }

        log.info("테스트 데이터 생성 완료 : {} 경기", count);
    }

    /**
     * Elasticsearch 동기화 이벤트 발행
     * @param savedMatches 저장된 경기 목록
     */
    private void publishEsSyncEvents(List<Match> savedMatches) {
        for (Match match : savedMatches) {
            matchEsSyncPublisher.publishMatchCreated(match.getId());
        }
        log.debug("ES 동기화 이벤트 발행 완료: {} 건", savedMatches.size());
    }
}