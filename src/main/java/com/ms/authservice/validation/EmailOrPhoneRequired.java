package com.ms.authservice.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = EmailOrPhoneValidator.class)
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface EmailOrPhoneRequired {

    String message() default "Either email or phone must be provided";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
