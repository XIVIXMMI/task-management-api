package com.omori.taskmanagement.dto.usermgmt;

import java.time.LocalDate;

public record UpdateUserProfileResponse(
                String message,
                String firstName,
                String lastName,
                String middleName,
                LocalDate dateOfBirth,
                String gender) {
}
