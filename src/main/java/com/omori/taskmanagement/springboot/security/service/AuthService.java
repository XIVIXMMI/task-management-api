package com.omori.taskmanagement.springboot.security.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AuthService {

    /**
     * Check if the current authenticated user has permission to access/modify data for the given username
     * @param username The username to check permission for
     * @return true if the current user has permission, false otherwise
     */
    public boolean hasPermission(String username) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("No authenticated user found when checking permission for username: {}", username);
            return false;
        }
        
        String currentUsername = authentication.getName();
        log.debug("Checking permission for user: {} to access data for: {}", currentUsername, username);
        
        // Allow users to access their own data
        boolean hasPermission = currentUsername.equals(username);
        
        if (!hasPermission) {
            log.warn("User {} attempted to access data for user {} without permission", currentUsername, username);
        } else {
            log.debug("Permission granted for user {} to access their own data", currentUsername);
        }
        
        return hasPermission;
    }
}
