package org.example.oddventure.domain.match.service;

import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

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
//import org.example.oddventure.domain.match.messaging.MatchEsSyncPublisher;
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
    private final MatchSearchService matchSearchService;
    //private final MatchEsSyncPublisher matchEsSyncPublisher;

    private final RedisTemplate<String, Object> redisTemplate;


    // 매치 생성 (매치별로 독립적인 트랜잭션 보유)
    @Transactional(propagation = REQUIRES_NEW)
    public MatchCreateDto createMatch(MatchScheduleDto dto) {
        boolean isExist = matchRepository.existsByFetchId(dto.fetchId());
        boolean isPending = dto.teamA().contains("TBD") || dto.teamB().contains("TBD");

        if (isExist || isPending) {
            throw new MatchException(MatchErrorCode.MATCH_NOT_CREATABLE);
        }

        Match match = Match.builder()
                .fetchId(dto.fetchId())
                .matchName(dto.matchName())
                .teamA(dto.teamA())
                .teamB(dto.teamB())
                .startTime(dto.startTime())
                .build();

        Match savedMatch = matchRepository.save(match);
       //matchEsSyncPublisher.publishMatchCreated(savedMatch.getId());
        matchEventProducer.produceMatchStartEvent(MatchStartEventDto.from(match.getId(), match.getStartTime()));

        return MatchCreateDto.builder().fetchId(dto.fetchId()).build();
    }

    // 매치 정보 수정
    @Transactional
    public MatchUpdateAdminResponse updateMatch(Long matchId, MatchUpdateRequest request) {
        Match match = findMatchById(matchId);

        match.update(request.matchName(), request.teamA(), request.teamB(), request.startTime(), request.status());

        //matchEsSyncPublisher.publishMatchUpdated(matchId);

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
        Match match = matchRepository.findByFetchId(fetchId)
                .orElseThrow(() -> new MatchException(MatchErrorCode.MATCH_NOT_FOUND));

        if (match.getStatus().equals(MatchStatus.FINISHED)) {
            throw new MatchException(MatchErrorCode.MATCH_FINISHED);
        }

        match.finishMatch(winner, loser);

    }

    @Transactional
    public void updateStatus(Long matchId, MatchStatus status) {
        Match match = findMatchById(matchId);
        match.setStatus(status);
    }

    private Match findMatchById(Long matchId) {
        return matchRepository.findById(matchId)
                .orElseThrow(() -> new MatchException(MatchErrorCode.MATCH_NOT_FOUND));
    }

    @Transactional
    public Page<MatchResponse> elasticSearchMatches(MatchSearchCondition condition, Pageable pageable) {

        return matchSearchService.searchMatches(condition, pageable);
    }
}