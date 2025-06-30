package com.omori.taskmanagement.springboot.security.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.omori.taskmanagement.springboot.security.jwt.JwtProperties;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SecurityService {

    private final JwtProperties jwtProperties;

    /**
     * Token expiration time in milliseconds (default 1 day)
     */
    public long getExpirationTime() {
        return jwtProperties.getExpirationMinute() * 60 * 1000;
    }

    /**
     * Secret key for JWT signature
     */
    public String getSecretKey() {
        return jwtProperties.getSecretKey();
    }

    /**
     * The company who provided token
     */
    public String getIssuer() {
        return jwtProperties.getIssuer();
    }

    /**
     * @return authenticated username from Security Context
     */
    public String getAuthenticatedUsername() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        final UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return userDetails.getUsername();
    }
}
