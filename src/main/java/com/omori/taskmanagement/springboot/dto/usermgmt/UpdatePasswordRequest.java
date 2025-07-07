package com.omori.taskmanagement.springboot.dto.usermgmt;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import com.omori.taskmanagement.springboot.validation.PasswordsMatch;

@PasswordsMatch
public record UpdatePasswordRequest(
        @NotBlank
        String oldPassword,
        @NotBlank
        @Size(min = 8, message = "Password must be at least 8 characters, and contain letters, numbers, and special characters.")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$",
                message = "Password must be at least 8 characters, and contain letters, numbers, and special characters.")
        String newPassword,
        @NotBlank
        String confirmPassword
) {}
