package org.example.oddventure.base;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.example.oddventure.domain.user.enums.UserRole;
import org.springframework.security.test.context.support.WithSecurityContext;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = TestSecurityContextFactory.class)
public @interface WithMockAuthUser {
    long userId();

    UserRole role();
}
