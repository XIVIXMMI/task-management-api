package com.omori.taskmanagement.springboot.security.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.antlr.v4.runtime.RuntimeMetaData;
import org.springframework.stereotype.Service;

import com.omori.taskmanagement.springboot.model.usermgmt.User;
import com.omori.taskmanagement.springboot.model.usermgmt.UserStatus;
import com.omori.taskmanagement.springboot.model.usermgmt.Role;
import com.omori.taskmanagement.springboot.model.usermgmt.Profile;
import com.omori.taskmanagement.springboot.model.usermgmt.Session;
import com.omori.taskmanagement.springboot.repository.usermgmt.RoleRepository;
import com.omori.taskmanagement.springboot.repository.usermgmt.UserRepository;
import com.omori.taskmanagement.springboot.repository.usermgmt.ProfileRepository;
import com.omori.taskmanagement.springboot.repository.usermgmt.SessionRepository;
import org.springframework.transaction.annotation.Transactional;
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
	private final UserValidationService userValidationService;
	private final GeneralMessageAccessor generalMessageAccessor;
	private final RoleRepository roleRepository;
	private final UserMapper mapper;
	private final ProfileRepository profileRepository;
	private final SessionRepository sessionRepository;

	@Override
	public User findByUsername(String username) {

		return userRepository.findByUsername(username);
	}

	@Override
	@Transactional
	public RegistrationResponse registration(RegistrationRequest registrationRequest) {
		userValidationService.validateUser(registrationRequest);

		final User user = mapper.convertToUser(registrationRequest);
		
		Role role = roleRepository
			.findById((short) 1)
			.orElseThrow(() -> new RuntimeException("Default role (ROLE_USER) is not found in database"));
			
		user.setRole(role);

		userRepository.save(user);

		// Create and save Profile for the new user
		Profile profile = Profile.builder()
			.firstName("guest") // or use registrationRequest.getFirstName() if available
			.lastName("") // or use registrationRequest.getLastName() if available
			.status(UserStatus.offline)
			.build();
		profileRepository.save(profile);

		// Link profile to user and save again
		user.setProfile(profile);
		userRepository.save(user);

		// Create and save Session for the new user
		Session session = Session.builder()
			.sessionId(java.util.UUID.randomUUID().toString())
			.user(user)
			.expiresAt(java.time.LocalDateTime.now().plusDays(1)) // 1 day expiry, adjust as needed
			.createdAt(java.time.LocalDateTime.now())
			.build();
		sessionRepository.save(session);

		final String username = registrationRequest.getUsername();
		final String registrationSuccessMessage = generalMessageAccessor.getMessage(null, REGISTRATION_SUCCESSFUL, username);

		log.info("{} registered successfully!", username);

		return new RegistrationResponse(registrationSuccessMessage);
	}

	@Override
	public AuthenticatedUserDto findAuthenticatedUserByUsername(String username) {

		final User user = findByUsername(username);

		return mapper.convertToAuthenticatedUserDto(user);
	}
}
