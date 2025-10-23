package org.example.oddventure.domain.user.enums;

import java.util.Arrays;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.oddventure.domain.user.exception.UserErrorCode;
import org.example.oddventure.domain.user.exception.UserException;

@Getter
@RequiredArgsConstructor
public enum UserRole {

    ROLE_USER(Authority.USER),
    ROLE_ADMIN(Authority.ADMIN);

    private final String userRole;

    public static UserRole of(String userRole) {
        return Arrays.stream(UserRole.values())
                .filter(r -> r.name().equalsIgnoreCase(userRole))
                .findFirst()
                .orElseThrow(() -> new UserException(UserErrorCode.INVALID_USER_ROLE));
    }

    public static class Authority {
        public static final String USER = "ROLE_USER";
        public static final String ADMIN = "ROLE_ADMIN";
    }
}