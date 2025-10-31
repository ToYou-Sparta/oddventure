package org.example.oddventure.domain.admin.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.oddventure.domain.admin.dto.response.MatchCreateAdminResponse;
import org.example.oddventure.domain.grid.dto.MatchResultDto;
import org.example.oddventure.domain.grid.dto.MatchScheduleDto;
import org.example.oddventure.domain.grid.service.GridService;
import org.example.oddventure.domain.match.dto.MatchCreateDto;
import org.example.oddventure.domain.match.entity.Match;
import org.example.oddventure.domain.match.exception.MatchException;
import org.example.oddventure.domain.match.repository.MatchRepository;
import org.example.oddventure.domain.match.service.MatchService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminMatchService {

    private final GridService gridService;
    private final MatchService matchService;
    private final MatchRepository matchRepository;

    // 매치 생성
    @Transactional
    public MatchCreateAdminResponse createMatch() {
        List<MatchScheduleDto> fetchResponses = gridService.fetchMatchSchedules();
        List<Long> fetchIds = new ArrayList<>();

        for (MatchScheduleDto respons : fetchResponses) {
            try {
                MatchCreateDto matchCreateDto = matchService.createMatch(respons);
                fetchIds.add(matchCreateDto.fetchId());
            } catch (MatchException e) {
                log.info("fetchId={}: {}", respons.fetchId(), e.getMessage());
            }
        }

        return MatchCreateAdminResponse.of(fetchIds);
    }

    // 매치 결과 연동
    @Transactional
    public void createMatchResult(Long fetchId) {
        MatchResultDto dto = gridService.fetchMatchResult(fetchId);

        matchService.updateMatchResult(fetchId, dto.winner(), dto.loser());
    }

    /**
     * 성능 테스트용 대량 더미 데이터 생성
     * 개발 환경에서만 사용
     *
     * @param count 생성할 매치 수
     */
    @Transactional
    public void generateTestData(int count) {

        String[] teams = {
                "Betera Esports", "Johnny Speeds", "Bestia", "Bounty Hunters",
                "Sharks", "Dusty Roots", "ARCRED", "SINNERS Esports",
                "Nexus Gaming", "AMKAL ESPORTS", "VP.Priodigy", "AaB Esport",
                "K27", "Dynamo Eclot", "JieJieHao", "9BOOMPRO",
                "Phantoms", "Montne", "ALGO", "The Glecs"
        };

        String[] matchNames = {
                "CCT S3 Europe Series 9", "CBCS Masters Xeque Mate 2025", "ECL Season 50 - Cup #4 Europe",
                "CCT S3 EU Se10", "ECL Season 50 - Cup #4 SA", "Rushzone CS2 October 2025"
        };

        Random random = new Random();
        List<Match> matches = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            // 1. 팀 A를 랜덤 선택
            String teamA = teams[random.nextInt(teams.length)];

            // 2. 팀 B를 선택 (단, 팀 A와 다르게)
            String teamB;
            do {
                teamB = matchNames[random.nextInt(matchNames.length)];
            } while (teamA.equals(teamB));

            // 3. 경기명 생성
            String matchName = matchNames[random.nextInt(matchNames.length)];

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

            // 5. 배치 저장 (1000씩 끊어서)
            if (matches.size() >= 1000) {
                matchRepository.saveAll(matches);
                matches.clear();
                log.info("배치 저장 : {} / {}", i + 1, count);
            }
        }

        if(!matches.isEmpty()) {
            matchRepository.saveAll(matches);
        }

        log.info("테스트 데이터 생성 완료 : {} 경기", count);
    }
}
