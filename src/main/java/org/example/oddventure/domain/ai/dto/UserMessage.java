package org.example.oddventure.domain.ai.dto;

import jakarta.validation.constraints.NotBlank;

public record UserMessage(
        @NotBlank(message = "메시지를 입력해주세요!")
        String message
) {
}
