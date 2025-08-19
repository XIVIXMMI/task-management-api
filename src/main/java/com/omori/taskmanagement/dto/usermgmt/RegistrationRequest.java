package com.omori.taskmanagement.dto.usermgmt;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class RegistrationRequest {

	@NotBlank(message = "{first_name_not_empty}")
	@Schema(description = "First name of the user", 
			example = "John",
			required = true)
	@Pattern(regexp = "^[a-zA-Z]+$", message = "{first_name_invalid}")
	private String firstName;

	@Schema(description = "Middle name of the user", 
			example = "Alexandro",
			required = false)
	@Pattern(regexp = "^[a-zA-Z]*$", message = "{middle_name_invalid}") // allows empty string
	private String middleName;

	@NotBlank(message = "{last_name_not_empty}")
	@Schema(description = "Last name of the user", 
			example = "Doe",
			required = true)
	@Pattern(regexp = "^[a-zA-Z]+$", message = "{last_name_invalid}")
	private String lastName;

	@NotBlank(message = "{mobile_not_empty}")
	@Schema(description = "Mobile number of the user", example = "1234567890", required = true)
	@Pattern(regexp = "^[0-9]+$", message = "{mobile_invalid}") // must be at least one digit
	private String mobile;
	
	@NotBlank(message = "{date_of_birth_not_empty}")
	@Schema(type = "string", 
			format = "date", 
			example = "1990-01-01",
			description = "Date of birth in format YYYY-MM-DD",
			required = true)
	private String dateOfBirth;

	@NotBlank(message = "{gender_not_empty}")
	@Schema(description = "Gender: M=Male, F=Female, O=Other, N=Not specified",
			example = "M",
			required = true)
	private String gender;

	@Schema(description = "Avatar path of the user",
			example = "https://example.com/avatar.png",
			required = false)
	private String avatarPath;

	@Schema(description = "Timezone of the user",
			example = "Asia/Ho_Chi_Minh",
			required = false)
	private String timezone;

	@Email(message = "{registration_email_is_not_valid}")
	@NotBlank(message = "{registration_email_not_empty}")
	@Schema(description = "Email of the user",
			example = "john.doe@example.com",
			required = true)
	private String email;

	@NotBlank(message = "{registration_username_not_empty}")
	@Schema(description = "Username of the user",
			example = "john.doe",
			required = true)
	private String username;

	@NotBlank(message = "{registration_password_not_empty}")
	@Schema(description = "Password must have uppercase, special and number character",
			example = "Password123!",
			required = true)
	private String password;

}
