package com.omori.taskmanagement.security.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.omori.taskmanagement.exceptions.RegistrationException;
import com.omori.taskmanagement.repository.usermgmt.UserRepository;
import com.omori.taskmanagement.dto.usermgmt.RegistrationRequest;
import com.omori.taskmanagement.utils.ExceptionMessageAccessor;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserValidationService {

    private static final String EMAIL_ALREADY_EXISTS = "email_already_exists";
    private static final String USERNAME_ALREADY_EXISTS = "username_already_exists";
    private static final String INVALID_EMAIL_FORMAT = "invalid_email_format";
    private static final String INVALID_PASSWORD_FORMAT = "invalid_password_format";
    private static final int PASSWORD_MIN_LENGTH = 8;
    private static final int PASSWORD_MAX_LENGTH = 32;
    // password must have uppercase, special and number character
    private static final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$";
    private static final String EMAIL_PATTERN = "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}";

    private final UserRepository userRepository;
    private final ExceptionMessageAccessor exceptionMessageAccessor;

    public void validateUser(RegistrationRequest registrationRequest) {
        final String email = registrationRequest.getEmail();
        final String username = registrationRequest.getUsername();
        final String password = registrationRequest.getPassword();

        checkEmail(email);
        checkUsername(username);
        checkPassword(password);
    }

    private void checkUsername(String username) {
        if (StringUtils.hasText(username)) {
            final boolean existsByUsername = userRepository.existsByUsername(username);

            if (existsByUsername) {
                log.warn("{} is already being used!", username);
                final String existsUsername = exceptionMessageAccessor.getMessage(null, USERNAME_ALREADY_EXISTS);
                throw new RegistrationException(existsUsername);
            }
        }
    }

    private void checkEmail(String email) {
        if (StringUtils.hasText(email)) {
            // Check email format
            if (!email.matches(EMAIL_PATTERN)) {
                throw new RegistrationException(exceptionMessageAccessor.getMessage(null, INVALID_EMAIL_FORMAT, email));
            }

            // Check if email already exists
            final boolean existsByEmail = userRepository.existsByEmail(email);

            if (existsByEmail) {
                log.warn("{} is already being used!", email);
                final String existsEmail = exceptionMessageAccessor.getMessage(null, EMAIL_ALREADY_EXISTS);
                throw new RegistrationException(existsEmail);
            }
        }
    }

    private void checkPassword(String password) {
        if (StringUtils.hasText(password)) {
            // Check password length
            if (password.length() < PASSWORD_MIN_LENGTH || password.length() > PASSWORD_MAX_LENGTH) {
                throw new RegistrationException(
                        exceptionMessageAccessor.getMessage(null, INVALID_PASSWORD_FORMAT, password));
            }

            // Check password pattern (must contain: uppercase, lowercase, number, special
            // character)
            if (!password.matches(PASSWORD_PATTERN)) {
                throw new RegistrationException(
                        exceptionMessageAccessor.getMessage(null, INVALID_PASSWORD_FORMAT, password));
            }
        }
    }
}
