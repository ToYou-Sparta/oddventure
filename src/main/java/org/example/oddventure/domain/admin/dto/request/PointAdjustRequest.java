package org.example.oddventure.domain.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record PointAdjustRequest(
        @NotNull BigDecimal amount,
        @NotBlank String reason
) {}