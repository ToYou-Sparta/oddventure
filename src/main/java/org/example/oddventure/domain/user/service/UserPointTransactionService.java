package org.example.oddventure.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.example.oddventure.domain.admin.dto.request.PointAdjustRequest;
import org.example.oddventure.domain.admin.exception.AdminErrorCode;
import org.example.oddventure.domain.admin.exception.AdminException;
import org.example.oddventure.domain.user.entity.User;
import org.example.oddventure.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 유저 포인트 관련 DB 트랜잭션만을 전담하는 서비스
 * 분산 락이 획득된 상태에서만 호출되어야 함
 */
@Service
@RequiredArgsConstructor
public class UserPointTransactionService {

    private final UserRepository userRepository;

    @Transactional
    public User increaseUserPoint(Long userId, PointAdjustRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AdminException(AdminErrorCode.USER_NOT_FOUND));

        user.plusPoint(request.amount());
        return user;
    }
}