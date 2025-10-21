package org.example.oddventure.domain.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.example.oddventure.domain.auth.validation.ValidPassword;

public record SignupRequest(

        @NotBlank(message = "회원이름은 필수입니다.")
        String username,

        @Email(message = "올바른 이메일 형식이 아닙니다.")
        String email,

        @NotBlank(message = "비밀번호는 필수입니다.")
        @ValidPassword
        String password
) {
}
