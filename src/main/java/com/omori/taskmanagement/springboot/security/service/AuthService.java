package com.omori.taskmanagement.springboot.security.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.omori.taskmanagement.springboot.model.usermgmt.User;
// import com.omori.taskmanagement.springboot.security.service.CustomUserDetails;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AuthService {

    /**
     * Check if the current authenticated user has permission to access/modify datea
     * for the given username
     * 
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

    /**
     * Get the currently authenticated user
     * 
     * @return The current User object, or null if no user is authenticated
     */
    public User getCurrentUser() {
        try {
            SecurityContext context = SecurityContextHolder.getContext();
            log.debug("SecurityContext: {}", context);
            
            if (context == null) {
                log.warn("SecurityContext is null");
                return null;
            }
            
            Authentication auth = context.getAuthentication();
            log.debug("Authentication object: {}", auth);
            
            if (auth == null) {
                log.warn("Authentication is null");
                return null;
            }
            
            Object principal = auth.getPrincipal();
            log.debug("Principal class: {}", principal != null ? principal.getClass().getName() : "null");
            log.debug("Principal: {}", principal);
            
            if (principal instanceof CustomUserDetails) {
                CustomUserDetails userDetails = (CustomUserDetails) principal;
                log.debug("Found authenticated user: {}", userDetails.getUsername());
                
                // Create a minimal User object with required fields
                return User.builder()
                        .id(userDetails.getId())
                        .username(userDetails.getUsername())
                        .passwordHash(userDetails.getPassword())
                        .uuid(java.util.UUID.randomUUID()) // Required field
                        .roleId((short) 1) // Default role ID, adjust as needed
                        .isActive(true) // Default to active
                        .build();
            } else if (principal instanceof String && "anonymousUser".equals(principal)) {
                log.warn("User is anonymous");
            } else if (principal != null) {
                log.warn("Unexpected principal type: {}", principal.getClass().getName());
            }
            
            return null;
        } catch (Exception e) {
            log.error("Error getting current user", e);
            return null;
        }
    }
}
