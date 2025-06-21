package com.omori.taskmanagement.springboot.security.service;

import com.omori.taskmanagement.springboot.model.User;
import com.omori.taskmanagement.springboot.security.dto.AuthenticatedUserDto;
import com.omori.taskmanagement.springboot.security.dto.RegistrationRequest;
import com.omori.taskmanagement.springboot.security.dto.RegistrationResponse;

public interface UserService {

	User findByUsername(String username);

	RegistrationResponse registration(RegistrationRequest registrationRequest);

	AuthenticatedUserDto findAuthenticatedUserByUsername(String username);

}
