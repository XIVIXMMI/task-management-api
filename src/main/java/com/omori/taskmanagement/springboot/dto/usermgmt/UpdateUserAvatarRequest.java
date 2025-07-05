package com.omori.taskmanagement.springboot.dto.usermgmt;

import jakarta.validation.constraints.NotBlank;
import com.omori.taskmanagement.springboot.validation.ValidImagePath;

public record UpdateUserAvatarRequest(
        @NotBlank
        @ValidImagePath
        String avatarPath
) {}