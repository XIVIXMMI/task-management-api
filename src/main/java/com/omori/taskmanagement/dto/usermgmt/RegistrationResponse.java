package com.omori.taskmanagement.dto.usermgmt;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationResponse {

	@Schema(description = "Registration message", example = "Registration successful")
	private String message;

}
