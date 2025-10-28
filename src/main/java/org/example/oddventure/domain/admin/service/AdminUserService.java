package org.example.oddventure.domain.admin.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.oddventure.domain.admin.dto.response.UserAdminResponse;
import org.example.oddventure.domain.admin.exception.AdminErrorCode;
import org.example.oddventure.domain.admin.exception.AdminException;
import org.example.oddventure.domain.user.entity.User;
import org.example.oddventure.domain.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;

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
}
