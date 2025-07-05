package com.omori.taskmanagement.springboot.dto.usermgmt;

import java.time.LocalDate;

public record UpdateUserProfileResponse(
                String message,
                String firstName,
                String lastName,
                String middleName,
                LocalDate dateOfBirth,
                String gender) {
}
