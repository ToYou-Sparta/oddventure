package org.example.oddventure.domain.admin.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.oddventure.domain.admin.dto.request.MatchCreateRequest;
import org.example.oddventure.domain.admin.dto.request.MatchUpdateRequest;
import org.example.oddventure.domain.admin.dto.request.PointAdjustRequest;
import org.example.oddventure.domain.admin.dto.response.MatchAdminResponse;
import org.example.oddventure.domain.admin.dto.response.PointAdjustResponse;
import org.example.oddventure.domain.admin.dto.response.UserAdminResponse;
import org.example.oddventure.domain.admin.exception.AdminErrorCode;
import org.example.oddventure.domain.admin.exception.AdminException;
import org.example.oddventure.domain.grid.dto.MatchResultDto;
import org.example.oddventure.domain.grid.dto.response.MatchFetchResponse;
import org.example.oddventure.domain.grid.service.GridService;
import org.example.oddventure.domain.match.entity.Match;
import org.example.oddventure.domain.match.repository.MatchRepository;
import org.example.oddventure.domain.match.service.MatchService;
import org.example.oddventure.domain.user.entity.User;
import org.example.oddventure.domain.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {

    private final MatchRepository matchRepository;
    private final UserRepository userRepository;
    private final GridService gridService;
    private final MatchService matchService;

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

    // 전체 사용자 목록 조회
    @Transactional(readOnly = true)
    public Page<UserAdminResponse> getAllUsers(String email, String username, Pageable pageable) {
        Page<User> users = userRepository.findBySearchConditions(email, username, pageable);
        return users.map(UserAdminResponse::from);
    }

    // 사용자 상세 조회
    @Transactional(readOnly = true)
    public UserAdminResponse getUserDetails(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AdminException(AdminErrorCode.USER_NOT_FOUND));

        return UserAdminResponse.from(user);
    }

    // 포인트 지급
    @Transactional
    public PointAdjustResponse adjustUserPoints(Long userId, PointAdjustRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AdminException(AdminErrorCode.USER_NOT_FOUND));

        user.plusPoint(request.amount());

        log.info("[ADMIN_POINT_ADJUSTMENT] userId={}, amount={}, reason='{}', finalBalance={}",
                userId, request.amount(), request.reason(), user.getPoint());

        return new PointAdjustResponse(
                user.getId(),
                user.getUsername(),
                request.amount(),
                user.getPoint()
        );
    }

    // 매치 일정 연동
    @Transactional
    public void fetchMatches() {
        List<MatchFetchResponse> fetchResponses = gridService.fetchMatches();

        fetchResponses.stream()
                .filter(dto -> !matchRepository.existsByFetchId(dto.fetchId()))
                .filter(dto -> !dto.teamA().contains("TBD")) // 미정된 경기
                .filter(dto -> !dto.teamB().contains("TBD"))
                .map(dto -> Match.builder()
                        .fetchId(dto.fetchId())
                        .matchName(dto.matchName())
                        .teamA(dto.teamA())
                        .teamB(dto.teamB())
                        .startTime(dto.startTime())
                        .build()).forEach(matchRepository::save);
    }

    // 매치 결과 연동
    @Transactional
    public void fetchMatchResult(Long fetchId) {
        MatchResultDto dto = gridService.fetchMatchResult(fetchId);

        matchService.updateMatchResult(fetchId, dto.winner(), dto.looser());
    }
}
