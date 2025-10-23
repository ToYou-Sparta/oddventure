package org.example.oddventure.common.aop;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.example.oddventure.domain.auth.dto.AuthUser;
import org.example.oddventure.domain.auth.exception.AuthErrorCode;
import org.example.oddventure.domain.auth.exception.AuthException;
import org.example.oddventure.domain.user.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class UserValidationAspect {

    private final UserRepository userRepository;

    @Before("@annotation(ValidUser)")
    public void validateUser() {
        AuthUser authUser = (AuthUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        userRepository.findById(authUser.id())
                .filter(u -> u.getUserRole() == authUser.userRole())
                .orElseThrow(() -> new AuthException(AuthErrorCode.ACCESS_TOKEN_MISMATCH));
    }
}
