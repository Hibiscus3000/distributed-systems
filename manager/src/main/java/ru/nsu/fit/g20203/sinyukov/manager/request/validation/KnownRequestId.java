package ru.nsu.fit.g20203.sinyukov.manager.request.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = RequestIdValidator.class)
public @interface KnownRequestId {

    String message() default "Unknown id";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
