package com.omori.taskmanagement.springboot.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import com.omori.taskmanagement.springboot.security.dto.UpdatePasswordRequest;

public class PasswordsMatchValidator implements ConstraintValidator<PasswordsMatch, UpdatePasswordRequest> {
    @Override
    public boolean isValid(UpdatePasswordRequest value, ConstraintValidatorContext context) {
        if (value == null) return true;
        String newPassword = value.newPassword();
        String confirmPassword = value.confirmPassword();
        if (newPassword == null || confirmPassword == null) return false;
        return newPassword.equals(confirmPassword);
    }
}
