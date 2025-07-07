package com.omori.taskmanagement.springboot.service;

import com.omori.taskmanagement.springboot.model.usermgmt.User;
import com.omori.taskmanagement.springboot.security.dto.AuthenticatedUserDto;
import com.omori.taskmanagement.springboot.dto.usermgmt.RegistrationRequest;
import com.omori.taskmanagement.springboot.dto.usermgmt.RegistrationResponse;

public interface UserService {

	User findByUsername(String username);

	RegistrationResponse registration(RegistrationRequest registrationRequest);

	AuthenticatedUserDto findAuthenticatedUserByUsername(String username);

}
