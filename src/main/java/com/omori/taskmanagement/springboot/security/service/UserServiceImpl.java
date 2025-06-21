package com.omori.taskmanagement.springboot.security.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.antlr.v4.runtime.RuntimeMetaData;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.omori.taskmanagement.springboot.model.User;
import com.omori.taskmanagement.springboot.model.UserRole;
import com.omori.taskmanagement.springboot.repository.UserRoleRepository;
import com.omori.taskmanagement.springboot.repository.UserRepository;
import com.omori.taskmanagement.springboot.security.dto.AuthenticatedUserDto;
import com.omori.taskmanagement.springboot.security.dto.RegistrationRequest;
import com.omori.taskmanagement.springboot.security.dto.RegistrationResponse;
import com.omori.taskmanagement.springboot.security.mapper.UserMapper;
import com.omori.taskmanagement.springboot.service.UserValidationService;
import com.omori.taskmanagement.springboot.utils.GeneralMessageAccessor;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

	private static final String REGISTRATION_SUCCESSFUL = "registration_successful";

	private final UserRepository userRepository;

	private final BCryptPasswordEncoder bCryptPasswordEncoder;

	private final UserValidationService userValidationService;

	private final GeneralMessageAccessor generalMessageAccessor;

	private final UserRoleRepository roleRepository;

	@Override
	public User findByUsername(String username) {

		return userRepository.findByUsername(username);
	}

	@Override
	public RegistrationResponse registration(RegistrationRequest registrationRequest) {

		userValidationService.validateUser(registrationRequest);

		final User user = UserMapper.INSTANCE.convertToUser(registrationRequest);
		user.setPasswordHash(bCryptPasswordEncoder.encode(user.getPasswordHash()));
		
		UserRole role = roleRepository
			.findById((short) 1)
			.orElseThrow( () -> new RuntimeException("Default role (ROLE_USER) is not found in database"));
			
		user.setRole(role);

		userRepository.save(user);

		final String username = registrationRequest.getUsername();
		final String registrationSuccessMessage = generalMessageAccessor.getMessage(null, REGISTRATION_SUCCESSFUL, username);

		log.info("{} registered successfully!", username);

		return new RegistrationResponse(registrationSuccessMessage);
	}

	@Override
	public AuthenticatedUserDto findAuthenticatedUserByUsername(String username) {

		final User user = findByUsername(username);

		return UserMapper.INSTANCE.convertToAuthenticatedUserDto(user);
	}
}
