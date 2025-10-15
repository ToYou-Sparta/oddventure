package org.example.oddventure.domain.match.service;

import lombok.RequiredArgsConstructor;
import org.example.oddventure.domain.match.dto.response.MatchResponse;
import org.example.oddventure.domain.match.entity.Match;
import org.example.oddventure.domain.match.enums.MatchStatus;
import org.example.oddventure.domain.match.repository.MatchRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MatchService {

    private final MatchRepository matchRepository;

    @Transactional(readOnly = true)
    public Page<MatchResponse> getMatches(Pageable pageable) {

        Page<Match> matches = matchRepository.findAll(pageable);

        return matches.map(MatchResponse::from);
    }

    @Transactional(readOnly = true)
    public MatchResponse getMatch(Long matchId) {

        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("해당 경기 일정을 찾을 수 없습니다."));

        return MatchResponse.from(match);
    }

    @Transactional(readOnly = true)
    public List<Match> searchMatches(String teamName, MatchStatus status) {
        return matchRepository.findByConditions(teamName, status);
    }
}
