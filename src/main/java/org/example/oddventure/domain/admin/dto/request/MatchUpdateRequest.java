package org.example.oddventure.domain.admin.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import org.example.oddventure.domain.match.enums.MatchStatus;

public record MatchUpdateRequest(
        @NotBlank String teamA,
        @NotBlank String teamB,
        @NotNull @Future LocalDateTime startTime,
        @NotNull MatchStatus status
) {}