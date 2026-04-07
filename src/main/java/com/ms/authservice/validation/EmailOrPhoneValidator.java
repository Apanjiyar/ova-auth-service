package com.ms.authservice.validation;

import com.ms.authservice.dto.RegisterRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class EmailOrPhoneValidator implements ConstraintValidator<EmailOrPhoneRequired, RegisterRequest> {

    @Override
    public boolean isValid(RegisterRequest request, ConstraintValidatorContext context) {

        if (request == null) return true;

        boolean hasEmail = request.getEmail() != null && !request.getEmail().trim().isEmpty();
        boolean hasPhone = request.getPhone() != null && !request.getPhone().trim().isEmpty();

        if (hasEmail || hasPhone) {
            return true;
        }

        // Custom error on fields instead of class
        context.disableDefaultConstraintViolation();

        context.buildConstraintViolationWithTemplate("Either email or phone is required")
                .addPropertyNode("email")
                .addConstraintViolation();

        context.buildConstraintViolationWithTemplate("Either email or phone is required")
                .addPropertyNode("phone")
                .addConstraintViolation();

        return false;
    }
}