package org.example.oddventure.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record WithdrawRequest(

        @NotBlank(message = "비밀번호는 필수 입력 값입니다.")
        String password
) {
}
