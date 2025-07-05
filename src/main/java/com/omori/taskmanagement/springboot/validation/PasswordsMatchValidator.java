package com.omori.taskmanagement.springboot.validation;

import com.omori.taskmanagement.springboot.security.dto.UpdatePasswordRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordsMatchValidator implements ConstraintValidator<PasswordsMatch, UpdatePasswordRequest> {
    @Override
    public boolean isValid(UpdatePasswordRequest value, ConstraintValidatorContext context) {
        if (value == null) return true; // let @NotNull handle nulls if needed
        String newPassword = value.newPassword();
        String confirmPassword = value.confirmPassword();
        return newPassword != null && newPassword.equals(confirmPassword);
    }
}
