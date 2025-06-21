package com.omori.taskmanagement.springboot.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.omori.taskmanagement.springboot.security.dto.RegistrationRequest;
import com.omori.taskmanagement.springboot.security.dto.RegistrationResponse;
import com.omori.taskmanagement.springboot.security.service.UserService;


@RestController
@RequiredArgsConstructor
@RequestMapping("/register")
public class RegistrationController {

	private final UserService userService;

	@PostMapping
	@Operation(tags = "Register Service", description = "You can register to the system by sending information in the appropriate format.")
	public ResponseEntity<RegistrationResponse> registrationRequest(@Valid @RequestBody RegistrationRequest registrationRequest) {

		final RegistrationResponse registrationResponse = userService.registration(registrationRequest);

		return ResponseEntity.status(HttpStatus.CREATED).body(registrationResponse);
	}

}
