package com.omori.taskmanagement.springboot.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.omori.taskmanagement.springboot.annotations.LogActivity;
import com.omori.taskmanagement.springboot.dto.common.ApiResponse;
import com.omori.taskmanagement.springboot.dto.usermgmt.LoginRequest;
import com.omori.taskmanagement.springboot.dto.usermgmt.LoginResponse;
import com.omori.taskmanagement.springboot.dto.usermgmt.RegistrationRequest;
import com.omori.taskmanagement.springboot.dto.usermgmt.RegistrationResponse;
import com.omori.taskmanagement.springboot.model.audit.ActionType;
import com.omori.taskmanagement.springboot.security.jwt.JwtTokenService;
import com.omori.taskmanagement.springboot.security.service.UserDetailsServiceImpl;
import com.omori.taskmanagement.springboot.service.UserService;

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
    private final UserDetailsServiceImpl userDetailsService;

    @LogActivity(ActionType.LOGIN)
	@PostMapping("/login")
    @Operation(summary = "Login", description = "Login to the system")
	public ResponseEntity<ApiResponse<LoginResponse>> loginRequest(
        @Valid 
        @RequestBody LoginRequest loginRequest) {
    
        UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getUsername());
        
        UsernamePasswordAuthenticationToken authentication = 
            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        
        log.debug("Security context set for user: {}", userDetails.getUsername());
        

		final LoginResponse loginResponse = jwtTokenService.getLoginResponse(loginRequest);
		return ResponseEntity.ok(ApiResponse.success(loginResponse));
	}

    @LogActivity(ActionType.REGISTER)
    @PostMapping("/register")
    @Operation(summary = "Register", description = "Register a new user")
    public ResponseEntity<ApiResponse<RegistrationResponse>> registerRequest(
        @Valid 
        @RequestBody RegistrationRequest registrationRequest) {

        final RegistrationResponse registrationResponse = userService.registration(registrationRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(registrationResponse));
    }
    
}
