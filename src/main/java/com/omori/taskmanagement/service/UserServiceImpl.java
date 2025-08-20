package com.omori.taskmanagement.service;

import com.omori.taskmanagement.security.service.UserValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import com.omori.taskmanagement.model.usermgmt.User;
import com.omori.taskmanagement.model.usermgmt.UserStatus;
import com.omori.taskmanagement.model.usermgmt.Role;
import com.omori.taskmanagement.model.usermgmt.Profile;
import com.omori.taskmanagement.model.usermgmt.Session;
import com.omori.taskmanagement.repository.usermgmt.RoleRepository;
import com.omori.taskmanagement.repository.usermgmt.UserRepository;
import com.omori.taskmanagement.repository.usermgmt.ProfileRepository;
import com.omori.taskmanagement.repository.usermgmt.UserSessionRepository;
import org.springframework.transaction.annotation.Transactional;
import com.omori.taskmanagement.security.dto.AuthenticatedUserDto;
import com.omori.taskmanagement.dto.usermgmt.RegistrationRequest;
import com.omori.taskmanagement.dto.usermgmt.RegistrationResponse;
import com.omori.taskmanagement.security.mapper.UserMapper;
import com.omori.taskmanagement.utils.GeneralMessageAccessor;

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
	private final UserSessionRepository sessionRepository;

	@Override
	public User findByUsername(String username) {

		return userRepository.findByUsername(username).orElse(null);
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
		user.setMobile(registrationRequest.getMobile());
		user.setEmail(registrationRequest.getEmail());
		userRepository.save(user);

		// Create and save Profile for the new user
		Profile profile = Profile.builder()
			.firstName(registrationRequest.getFirstName())
			.middleName(registrationRequest.getMiddleName())
			.lastName(registrationRequest.getLastName())
			.dateOfBirth(java.time.LocalDate.parse(registrationRequest.getDateOfBirth()))
			.gender(registrationRequest.getGender())
			.avatarPath(registrationRequest.getAvatarPath())
			.timezone(registrationRequest.getTimezone())
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
