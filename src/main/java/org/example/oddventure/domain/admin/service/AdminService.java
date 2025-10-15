package org.example.oddventure.domain.admin.service;

import lombok.RequiredArgsConstructor;
import org.example.oddventure.common.exception.GlobalException;
import org.example.oddventure.domain.admin.dto.request.MatchCreateRequest;
import org.example.oddventure.domain.admin.dto.request.MatchUpdateRequest;
import org.example.oddventure.domain.admin.dto.response.MatchAdminResponse;
import org.example.oddventure.domain.match.entity.Match;
import org.example.oddventure.domain.match.repository.MatchRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static org.example.oddventure.domain.admin.exception.AdminErrorCode.MATCH_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final MatchRepository matchRepository;

    // 매치 생성
    @Transactional
    public MatchAdminResponse createMatch(MatchCreateRequest request) {
        Match match = Match.builder()
                .teamA(request.teamA())
                .teamB(request.teamB())
                .startTime(request.startTime())
                .build();

        Match savedMatch = matchRepository.save(match);
        return MatchAdminResponse.fromEntity(savedMatch);
    }

    // 매치 상태 수정
    @Transactional
    public MatchAdminResponse updateMatch(Long matchId, MatchUpdateRequest request) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new GlobalException(MATCH_NOT_FOUND));

        match.update(
                request.teamA(),
                request.teamB(),
                request.startTime(),
                request.status()
        );

        return MatchAdminResponse.fromEntity(match);
    }
}