package com.omori.taskmanagement.springboot.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Constraint(validatedBy = ValidImagePathValidator.class)
@Target({ FIELD })
@Retention(RUNTIME)
public @interface ValidImagePath {
    String message() default "Avatar path must be a valid image file path or URL (.jpg, .jpeg, .png, .gif)";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
