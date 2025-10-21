package org.example.oddventure.domain.admin.service;


import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.oddventure.domain.admin.dto.request.InitialOddsSetRequest;
import org.example.oddventure.domain.admin.dto.request.MatchCreateRequest;
import org.example.oddventure.domain.admin.dto.request.MatchUpdateRequest;
import org.example.oddventure.domain.admin.dto.request.PointAdjustRequest;
import org.example.oddventure.domain.admin.dto.response.MatchAdminResponse;
import org.example.oddventure.domain.admin.dto.response.PointAdjustResponse;
import org.example.oddventure.domain.admin.dto.response.UserAdminResponse;
import org.example.oddventure.domain.admin.exception.AdminErrorCode;
import org.example.oddventure.domain.admin.exception.InvalidAdminException;
import org.example.oddventure.domain.match.entity.Match;
import org.example.oddventure.domain.match.repository.MatchRepository;
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
                .orElseThrow(() -> new InvalidAdminException(AdminErrorCode.MATCH_NOT_FOUND));

        match.update(
                request.matchName(),
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
    public UserAdminResponse getUserDetails(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new InvalidAdminException(AdminErrorCode.USER_NOT_FOUND));

        return UserAdminResponse.fromEntity(user);
    }

    // 포인트 지급
    public PointAdjustResponse adjustUserPoints(Long userId, PointAdjustRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new InvalidAdminException(AdminErrorCode.USER_NOT_FOUND));

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

    // 초기 배당률 설정
    public MatchAdminResponse setInitialOdds(Long matchId, InitialOddsSetRequest request)
    {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new InvalidAdminException(AdminErrorCode.MATCH_NOT_FOUND));

        // 베팅 총액이 0보다 크면, 이미 베팅이 시작된 것이므로 예외 발생
        boolean hasBets = match.getTotalAmountA().add(match.getTotalAmountB())
                .compareTo(BigDecimal.ZERO) > 0;
        if (hasBets) {
            throw new InvalidAdminException(AdminErrorCode.CANNOT_SET_INITIAL_ODDS);
        }

        match.updateInitialOdds(request.oddsA(), request.oddsB());

        return MatchAdminResponse.fromEntity(match);
    }
}
