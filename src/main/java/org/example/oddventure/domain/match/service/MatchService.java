package org.example.oddventure.domain.match.service;

import lombok.RequiredArgsConstructor;
import org.example.oddventure.domain.match.dto.projection.MatchProjection;
import org.example.oddventure.domain.match.dto.request.MatchSearchCondition;
import org.example.oddventure.domain.match.dto.response.MatchResponse;
import org.example.oddventure.domain.match.entity.Match;
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

    @Transactional(readOnly = true)
    public Page<MatchResponse> getMatches(Pageable pageable) {

        Page<Match> matches = matchRepository.findAll(pageable);

        return matches.map(MatchResponse::from);
    }

    @Transactional
    public MatchResponse getMatch(Long matchId) {

        matchRepository.incrementViewCount(matchId);
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new MatchException(MatchErrorCode.MATCH_NOT_FOUND));

        return MatchResponse.from(match);
    }

    @Transactional(readOnly = true)
    public Page<MatchResponse> searchMatches(MatchSearchCondition condition, Pageable pageable) {

        Page<MatchProjection> projections = matchRepository.searchByCondition(condition, pageable);

        return projections.map(MatchResponse::from);
    }
}
