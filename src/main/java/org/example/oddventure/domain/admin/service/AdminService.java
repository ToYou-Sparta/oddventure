package org.example.oddventure.domain.admin.service;

import lombok.RequiredArgsConstructor;
import org.example.oddventure.common.exception.GlobalException;
import org.example.oddventure.domain.admin.dto.request.MatchCreateRequest;
import org.example.oddventure.domain.admin.dto.request.MatchUpdateRequest;
import org.example.oddventure.domain.admin.dto.response.MatchAdminResponse;
import org.example.oddventure.domain.admin.dto.response.UserAdminResponse;
import org.example.oddventure.domain.match.entity.Match;
import org.example.oddventure.domain.match.repository.MatchRepository;
import org.example.oddventure.domain.user.entity.User;
import org.example.oddventure.domain.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static org.example.oddventure.domain.admin.exception.AdminErrorCode.MATCH_NOT_FOUND;
import static org.example.oddventure.domain.admin.exception.AdminErrorCode.USER_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final MatchRepository matchRepository;
    private final UserRepository userRepository;

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

    // 전체 사용자 목록 조회
    @Transactional(readOnly = true)
    public Page<UserAdminResponse> getAllUsers(String email, String username, Pageable pageable) {
        Page<User> users = userRepository.findBySearchConditions(email, username, pageable);
        return users.map(UserAdminResponse::fromEntity);
    }

    // 사용자 상세 조회
    @Transactional(readOnly = true)
    public UserAdminResponse getUserDetails(Long userId)
    {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GlobalException(USER_NOT_FOUND));

        return UserAdminResponse.fromEntity(user);
    }
}