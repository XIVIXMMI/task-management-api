package com.omori.taskmanagement.springboot.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.omori.taskmanagement.springboot.security.dto.LoginRequest;
import com.omori.taskmanagement.springboot.security.dto.LoginResponse;
import com.omori.taskmanagement.springboot.security.dto.RegistrationRequest;
import com.omori.taskmanagement.springboot.security.dto.RegistrationResponse;
import com.omori.taskmanagement.springboot.security.jwt.JwtTokenService;
import com.omori.taskmanagement.springboot.security.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/auth")
@Slf4j
@Tag(name = "Auth", description = "Authentication API")
public class AuthController {

    private final JwtTokenService jwtTokenService;
    private final UserService userService;

	@PostMapping("/login")
    @Operation(summary = "Login", description = "Login to the system")
	public ResponseEntity<LoginResponse> loginRequest(
        @Valid 
        @RequestBody LoginRequest loginRequest) {

		final LoginResponse loginResponse = jwtTokenService.getLoginResponse(loginRequest);

		return ResponseEntity.ok(loginResponse);
	}

    @PostMapping("/register")
    @Operation(summary = "Register", description = "Register a new user")
    public ResponseEntity<RegistrationResponse> registerRequest(
        @Valid 
        @RequestBody RegistrationRequest registrationRequest) {

        final RegistrationResponse registrationResponse = userService.registration(registrationRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(registrationResponse);
    }
    
}
