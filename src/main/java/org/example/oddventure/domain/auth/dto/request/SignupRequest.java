package org.example.oddventure.domain.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.example.oddventure.domain.auth.validation.ValidPassword;

public record SignupRequest(

        @NotBlank(message = "회원이름은 필수 입력 값입니다.")
        @Size(min = 2, max = 10, message = "사용자 이름은 2자 이상 10자 이하로 입력해주세요.")
        String username,

        @Email(message = "올바른 이메일 형식이 아닙니다.")
        String email,

        @NotBlank(message = "비밀번호는 필수 입력 값입니다.")
        @ValidPassword
        String password
) {
}
