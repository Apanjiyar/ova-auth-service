package com.ms.authservice.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {

        if (password == null) return false;

        boolean hasUpper = password.matches(".*[A-Z].*");
        boolean hasLower = password.matches(".*[a-z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSpecial = password.matches(".*[@#$%^&+=!].*");
        boolean hasLength = password.length() >= 8;

        if (hasUpper && hasLower && hasDigit && hasSpecial && hasLength) {
            return true;
        }

        context.disableDefaultConstraintViolation();

        if (!hasUpper)
            context.buildConstraintViolationWithTemplate("Must contain uppercase letter").addConstraintViolation();

        if (!hasLower)
            context.buildConstraintViolationWithTemplate("Must contain lowercase letter").addConstraintViolation();

        if (!hasDigit)
            context.buildConstraintViolationWithTemplate("Must contain number").addConstraintViolation();

        if (!hasSpecial)
            context.buildConstraintViolationWithTemplate("Must contain special character").addConstraintViolation();

        if (!hasLength)
            context.buildConstraintViolationWithTemplate("Must be at least 8 characters").addConstraintViolation();

        return false;
    }
}
