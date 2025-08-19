package com.omori.taskmanagement.dto.usermgmt;

import jakarta.validation.constraints.NotBlank;
import com.omori.taskmanagement.validation.ValidImagePath;

public record UpdateUserAvatarRequest(
        @NotBlank
        @ValidImagePath
        String avatarPath
) {}