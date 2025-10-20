package org.example.oddventure.domain.auth.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {

    private static final String PASSWORD_PATTERN =
            "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=]).{8,20}$";

    private Pattern pattern;

    @Override
    public void initialize(ValidPassword constraintAnnotation) {
        pattern = Pattern.compile(PASSWORD_PATTERN);
    }

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null) {
            return false;
        }
        return pattern.matcher(password).matches();
    }
}
