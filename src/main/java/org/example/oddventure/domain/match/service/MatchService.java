package org.example.oddventure.domain.match.service;

import lombok.RequiredArgsConstructor;
import org.example.oddventure.domain.admin.dto.request.MatchCreateRequest;
import org.example.oddventure.domain.admin.dto.request.MatchUpdateRequest;
import org.example.oddventure.domain.admin.dto.response.MatchAdminResponse;
import org.example.oddventure.domain.admin.exception.AdminErrorCode;
import org.example.oddventure.domain.admin.exception.AdminException;
import org.example.oddventure.domain.grid.dto.MatchScheduleDto;
import org.example.oddventure.domain.hotKeywords.service.HotKeywordsService;
import org.example.oddventure.domain.match.dto.projection.MatchProjection;
import org.example.oddventure.domain.match.dto.request.MatchSearchCondition;
import org.example.oddventure.domain.match.dto.response.MatchResponse;
import org.example.oddventure.domain.match.entity.Match;
import org.example.oddventure.domain.match.enums.MatchStatus;
import org.example.oddventure.domain.match.exception.MatchErrorCode;
import org.example.oddventure.domain.match.exception.MatchException;
import org.example.oddventure.domain.match.repository.MatchRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MatchService {

    private final MatchRepository matchRepository;
    private final HotKeywordsService hotKeywordsService;

    // 매치 생성
    @Transactional
    public MatchAdminResponse createMatch(MatchCreateRequest request) {
        Match match = Match.builder()
                .matchName(request.matchName())
                .teamA(request.teamA())
                .teamB(request.teamB())
                .startTime(request.startTime())
                .build();
        Match savedMatch = matchRepository.save(match);

        return MatchAdminResponse.from(savedMatch);
    }

    // 매치 정보 수정
    @Transactional
    public MatchAdminResponse updateMatch(Long matchId, MatchUpdateRequest request) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new AdminException(AdminErrorCode.MATCH_NOT_FOUND));

        match.update(
                request.matchName(),
                request.teamA(),
                request.teamB(),
                request.startTime(),
                request.status()
        );

        return MatchAdminResponse.from(match);
    }

    @Transactional(readOnly = true)
    public Page<MatchResponse> getMatches(Pageable pageable) {
        Page<Match> matches = matchRepository.findAll(pageable);
        return matches.map(MatchResponse::from);
    }

    @Transactional
    public MatchResponse getMatch(Long matchId) {
        int updated = matchRepository.incrementViewCount(matchId);
        if (updated == 0) {
            throw new MatchException(MatchErrorCode.MATCH_NOT_FOUND);
        }

        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new MatchException(MatchErrorCode.MATCH_NOT_FOUND));

        return MatchResponse.from(match);
    }

    @Transactional
    public Page<MatchResponse> searchMatches(MatchSearchCondition condition, Pageable pageable) {
        Page<MatchProjection> projections = matchRepository.searchByCondition(condition, pageable);

        hotKeywordsService.incrementSearchScore(condition.keyword());

        return projections.map(MatchResponse::of);
    }

    @Transactional
    public void fetchMatches(MatchScheduleDto dto) {
        boolean isExist = matchRepository.existsByFetchId(dto.fetchId());
        boolean isPending = dto.teamA().contains("TBD") || dto.teamB().contains("TBD");

        if (!isExist && !isPending) {
            Match match = Match.builder()
                    .fetchId(dto.fetchId())
                    .matchName(dto.matchName())
                    .teamA(dto.teamA())
                    .teamB(dto.teamB())
                    .startTime(dto.startTime())
                    .build();

            matchRepository.save(match);
        }
    }

    @Transactional
    public void updateMatchResult(Long fetchId, String winner, String looser) {
        Match match = matchRepository.findByFetchId(fetchId)
                .orElseThrow(() -> new MatchException(MatchErrorCode.MATCH_NOT_FOUND));

        if (match.getStatus().equals(MatchStatus.FINISHED)) {
            throw new MatchException(MatchErrorCode.MATCH_FINISHED);
        }

        match.finishMatch(winner, looser);

    }
}