package com.omori.taskmanagement.springboot.dto.usermgmt;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UpdateUserAvatarRequest(
        @NotBlank
        @Pattern( regexp = "^(https?:\\/\\/[^\\s\"']+\\.(jpg|jpeg|png|gif)" +
                        "|\\/[^\\s\"']+\\.(jpg|jpeg|png|gif)" +
                        "|[a-zA-Z0-9_\\-\\/]+\\/[^\\s\"']+\\.(jpg|jpeg|png|gif))$",
                message = "Avatar path must be a valid image URL or path (.jpg, .jpeg, .png, .gif)"
        )
        String avatarPath
) {}