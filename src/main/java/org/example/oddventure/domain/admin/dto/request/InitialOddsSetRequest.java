package org.example.oddventure.domain.admin.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record InitialOddsSetRequest(
        @NotNull @Positive BigDecimal oddsA,
        @NotNull @Positive BigDecimal oddsB
) {}