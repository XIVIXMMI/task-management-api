package com.omori.taskmanagement.springboot.dto.usermgmt;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
public class LoginRequest {

	@NotEmpty(message = "{login_username_not_empty}")
	@Schema(description = "Username of the user", 
				example = "john.doe",
				required = true)
	private String username;

	@NotEmpty(message = "{login_password_not_empty}")
	@Schema(description = "Password of the user", 
				example = "Abc@1234",
				required = true)
	private String password;

}
