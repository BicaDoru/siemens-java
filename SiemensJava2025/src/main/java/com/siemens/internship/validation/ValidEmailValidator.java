package com.siemens.internship.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class ValidEmailValidator implements ConstraintValidator<ValidEmail, String> {

    // // Validates email addresses with alphanumeric characters, optional symbols before '@', and a valid domain.
    private static final String EMAIL_REGEX =
            "^[A-Za-z0-9]+(?:[._%+-][A-Za-z0-9]+)*@" +
                    "[A-Za-z0-9]+(?:-[A-Za-z0-9]+)*(?:\\.[A-Za-z]{2,})+$";

    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        // Returns true if email is not null and matches the pattern
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

}
