package com.omori.taskmanagement.springboot.dto.usermgmt;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class UpdateUserProfileRequest {

    @NotBlank(message = "{first_name_not_empty}")
    @Schema(description = "First name of the user", example = "Anthony")
    private String firstName;

    @NotBlank( message = "{last_name_not_empty}")
    @Schema( description = "Last name of the user",example = "Stark")
    private String lastName;

    @Schema( description = "Middle name of the user", example = "Edward")
    private String middleName;

    @NotNull(message = "{date_of_birth_not_empty}")
    @Schema( description = "Date of birth in format YYYY-MM-DD",
            type = "string",
            format = "date",
            example = "1970-05-29")
    private LocalDate dateOfBirth;

    @NotBlank(message = "{gender_not_empty}")
    @Schema(description = "Gender: M=Male, F=Female, O=Other, N=Not specified", example = "M")
    private String gender;

}
