package com.omori.taskmanagement.springboot.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidImagePathValidator implements ConstraintValidator<ValidImagePath, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank())
            return false;
        String lower = value.toLowerCase();
        return lower.matches("^(https?://.+\\.(jpg|jpeg|png|gif))$") ||
                lower.matches("^/[a-zA-Z0-9_.\\-/]+\\.(jpg|jpeg|png|gif)$") ||
                lower.matches("^[a-zA-Z0-9_.\\-]+\\.(jpg|jpeg|png|gif)$");
    }
}
