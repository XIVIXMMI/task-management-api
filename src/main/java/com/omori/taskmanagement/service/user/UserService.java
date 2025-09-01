package com.omori.taskmanagement.service.user;

import com.omori.taskmanagement.model.usermgmt.User;
import com.omori.taskmanagement.security.dto.AuthenticatedUserDto;
import com.omori.taskmanagement.dto.usermgmt.RegistrationRequest;
import com.omori.taskmanagement.dto.usermgmt.RegistrationResponse;

public interface UserService {

	User findByUsername(String username);

	RegistrationResponse registration(RegistrationRequest registrationRequest);

	AuthenticatedUserDto findAuthenticatedUserByUsername(String username);

}
