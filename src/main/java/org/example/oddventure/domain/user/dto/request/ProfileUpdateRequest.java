package org.example.oddventure.domain.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record ProfileUpdateRequest(

        @Size(min = 2, max = 10, message = "사용자 이름은 2자 이상 10자 이하로 입력해주세요.")
        String username,

        @Email(message = "올바른 이메일 형식이 아닙니다.")
        String email
) {
}