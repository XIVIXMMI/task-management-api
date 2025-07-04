package com.omori.taskmanagement.springboot.security.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UpdateMobileRequest (
        @NotBlank
        String oldPhoneNumber,
        @NotBlank
        @Pattern(regexp = "^[0-9]+$", message = "{mobile_invalid}")
        String newPhoneNumber
){
    // maybe add OPT field in future for more secure
}
