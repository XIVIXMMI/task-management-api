package com.omori.taskmanagement.springboot.security.service;

import com.omori.taskmanagement.springboot.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.omori.taskmanagement.springboot.model.usermgmt.Role;

import java.util.Collections;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

	private static final String USERNAME_OR_PASSWORD_INVALID = "Invalid username or password.";

	private final UserService userService;

	@Override
	public UserDetails loadUserByUsername(String username) {

		final com.omori.taskmanagement.springboot.model.usermgmt.User user = userService.findByUsername(username);

		if (Objects.isNull(user)) {
			throw new UsernameNotFoundException(USERNAME_OR_PASSWORD_INVALID);
		}

		Long userId = user.getId();
		String authenticatedUsername = user.getUsername();
		String authenticatedPassword = user.getPasswordHash();
		Role userRole = user.getRole();

		SimpleGrantedAuthority authority = new SimpleGrantedAuthority(userRole.getName());

		return new CustomUserDetails(
				userId,
				authenticatedUsername,
				authenticatedPassword,
				Collections.singletonList(authority),
				true, // isEnabled
				true, // isAccountNonExpired
				true, // isAccountNonLocked
				true  // isCredentialsNonExpired
		);
	}

}
