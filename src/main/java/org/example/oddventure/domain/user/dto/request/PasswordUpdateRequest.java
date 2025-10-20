package org.example.oddventure.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import org.example.oddventure.domain.auth.validation.ValidPassword;

public record PasswordUpdateRequest(
        @NotBlank String currentPassword,
        @NotBlank @ValidPassword String newPassword
) {}