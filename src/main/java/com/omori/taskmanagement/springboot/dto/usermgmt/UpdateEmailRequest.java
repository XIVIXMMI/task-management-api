package com.omori.taskmanagement.springboot.dto.usermgmt;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UpdateEmailRequest(

        @NotBlank
        @Pattern(regexp = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$",
                message = "{registration_email_is_not_valid}")
        String oldEmail,

        @NotBlank
        @Pattern(regexp = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$",
                message = "{registration_email_is_not_valid}")
        String newEmail
) {
    // maybe add email verified in the future
}
