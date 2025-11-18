package org.example.oddventure.domain.match.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.oddventure.domain.admin.dto.request.MatchUpdateRequest;
import org.example.oddventure.domain.admin.dto.response.MatchUpdateAdminResponse;
import org.example.oddventure.domain.grid.dto.MatchScheduleDto;
import org.example.oddventure.domain.hotKeywords.service.HotKeywordsService;
import org.example.oddventure.domain.match.dto.MatchCreateDto;
import org.example.oddventure.domain.match.dto.event.MatchStartEventDto;
import org.example.oddventure.domain.match.dto.projection.MatchProjection;
import org.example.oddventure.domain.match.dto.request.MatchSearchCondition;
import org.example.oddventure.domain.match.dto.response.MatchResponse;
import org.example.oddventure.domain.match.entity.Match;
import org.example.oddventure.domain.match.enums.MatchStatus;
import org.example.oddventure.domain.match.event.MatchEventProducer;
import org.example.oddventure.domain.match.exception.MatchErrorCode;
import org.example.oddventure.domain.match.exception.MatchException;
import org.example.oddventure.domain.match.event.MatchEsSyncPublisher;
import org.example.oddventure.domain.match.repository.MatchJdbcRepository;
import org.example.oddventure.domain.match.repository.MatchRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchService {

    private final MatchRepository matchRepository;
    private final HotKeywordsService hotKeywordsService;
    private final MatchEventProducer matchEventProducer;
    private final MatchJdbcRepository matchJdbcRepository;
    private final MatchSearchService matchSearchService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final MatchEsSyncPublisher esSyncPublisher;


    // 매치 생성 (배치 적용)
    @Transactional
    public List<MatchCreateDto> createMatch(List<MatchScheduleDto> fetchedList) {
        List<Long> allFetchIds = fetchedList.stream()
                .map(MatchScheduleDto::fetchId)
                .toList();
        List<Long> existingFetchIds = matchRepository.findExistingFetchIds(allFetchIds);
        Set<Long> existingSet = new HashSet<>(existingFetchIds);

        List<Match> toSave = new ArrayList<>();
        for (MatchScheduleDto dto : fetchedList) {
            if (existingSet.contains(dto.fetchId())) {
                log.info("이미 생성된 매치입니다. fetchId: {}", dto.fetchId());
                continue;
            }
            if (dto.teamA().contains("TBD") || dto.teamB().contains("TBD")) {
                log.info("미정된 매치입니다. fetchId: {}", dto.fetchId());
                continue;
            }

            toSave.add(Match.builder()
                    .fetchId(dto.fetchId())
                    .matchName(dto.matchName())
                    .teamA(dto.teamA())
                    .teamB(dto.teamB())
                    .startTime(dto.startTime())
                    .build());
        }

        matchJdbcRepository.saveAllMatches(toSave);

        toSave.stream().map(match -> MatchStartEventDto.from(match.getFetchId(), match.getStartTime()))
                .forEach(matchEventProducer::produceMatchStartEvent);

        // Elasticsearch 동기화 이벤트 발행 (생성)
        toSave.forEach(match -> esSyncPublisher.publishMatchCreated(match.getId()));

        return toSave.stream().map(MatchCreateDto::from).toList();
    }

    // 매치 정보 수정
    @Transactional
    public MatchUpdateAdminResponse updateMatch(Long matchId, MatchUpdateRequest request) {
        Match match = findMatchById(matchId);

        match.update(request.matchName(), request.teamA(), request.teamB(), request.startTime(), request.status());

        // Elasticsearch 동기화 이벤트 발행 (업데이트)
        esSyncPublisher.publishMatchUpdated(matchId);

        return MatchUpdateAdminResponse.from(match);
    }

    @Transactional(readOnly = true)
    public Page<MatchResponse> getMatches(Pageable pageable) {
        Page<Match> matches = matchRepository.findAll(pageable);
        return matches.map(MatchResponse::from);
    }

    @Cacheable(value = "matchDetails", key = "#matchId", unless = "#result.status.name() != 'FINISHED'")
    @Transactional(readOnly = true)
    public MatchResponse getMatch(Long matchId) {
        log.info("getMatch for matchId: {}", matchId);
        Match match = findMatchById(matchId);
        return MatchResponse.from(match);
    }

    // 조회수 증가 로직
    public void incrementViewCount(Long matchId) {
        String viewCountKey = "match:viewcount:" + matchId;
        redisTemplate.opsForValue().increment(viewCountKey);
    }

    @Transactional
    public void updateViewCount(Long matchId, Long viewCount) {
        int updated = matchRepository.updateViewCount(matchId, viewCount);
        if (updated == 0) {
            log.warn("DB에 matchId: {}가 존재하지 않아 조회수 동기화 실패", matchId);
        }
    }

    @Transactional
    public Page<MatchResponse> searchMatches(MatchSearchCondition condition, Pageable pageable) {
        Page<MatchProjection> projections = matchRepository.searchByCondition(condition, pageable);

        hotKeywordsService.incrementSearchScore(condition.keyword());

        return projections.map(MatchResponse::of);
    }

    @Transactional
    public void updateMatchResult(Long fetchId, String winner, String loser) {
        Match match = findByFetchId(fetchId);

        if (match.getStatus().equals(MatchStatus.FINISHED)) {
            throw new MatchException(MatchErrorCode.MATCH_FINISHED);
        }

        match.finishMatch(winner, loser);

        // Elasticsearch 동기화 이벤트 발행 (경기 결과 업데이트)
        esSyncPublisher.publishMatchUpdated(match.getId());
    }

    @Transactional
    public void updateStatus(Long fetchId, MatchStatus status) {
        Match match = findByFetchId(fetchId);
        match.setStatus(status);

        // Elasticsearch 동기화 이벤트 발행 (상태 업데이트)
        esSyncPublisher.publishMatchUpdated(match.getId());
    }

    private Match findMatchById(Long matchId) {
        return matchRepository.findById(matchId)
                .orElseThrow(() -> new MatchException(MatchErrorCode.MATCH_NOT_FOUND));
    }

    private Match findByFetchId(Long fetchId) {
        return matchRepository.findByFetchId(fetchId)
                .orElseThrow(() -> new MatchException(MatchErrorCode.MATCH_NOT_FOUND));
    }

    @Transactional
    public Page<MatchResponse> elasticSearchMatches(MatchSearchCondition condition, Pageable pageable) {

        return matchSearchService.searchMatches(condition, pageable);
    }
}