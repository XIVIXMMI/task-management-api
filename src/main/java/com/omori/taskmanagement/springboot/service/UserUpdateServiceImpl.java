package com.omori.taskmanagement.springboot.service;


import org.springframework.stereotype.Service;

import com.omori.taskmanagement.springboot.dto.usermgmt.UpdateUserAvatarRequest;
import com.omori.taskmanagement.springboot.dto.usermgmt.UpdateUserProfileRequest;
import com.omori.taskmanagement.springboot.exceptions.UserNotFoundException;
import com.omori.taskmanagement.springboot.exceptions.UserProfileNotFoundException;
import com.omori.taskmanagement.springboot.model.usermgmt.Profile;
import com.omori.taskmanagement.springboot.model.usermgmt.User;
import java.time.LocalDateTime;
import java.sql.Timestamp;
import com.omori.taskmanagement.springboot.repository.usermgmt.UserRepository;
import com.omori.taskmanagement.springboot.repository.usermgmt.ProfileRepository;
import com.omori.taskmanagement.springboot.security.dto.UpdateEmailRequest;
import com.omori.taskmanagement.springboot.security.dto.UpdatePasswordRequest;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserUpdateServiceImpl implements UserUpdateService {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;

    @Override
    @Transactional
    public User updateProfile(String username, UpdateUserProfileRequest request) {
        log.info("Updating profile for user: {}", username);
        
        User user = userRepository
            .findByUsername(username)
            .orElseThrow(() -> new UserNotFoundException("User " + username + " not found"));

        Profile profile = user.getProfile();
        if (profile == null) {
            throw new UserProfileNotFoundException("Profile not found for user " + username);
        }

        log.info("Profile found with ID: {}", profile.getId());
        
        // Update profile fields
        profile.setFirstName(request.getFirstName());
        profile.setMiddleName(request.getMiddleName());
        profile.setLastName(request.getLastName());
        profile.setDateOfBirth(request.getDateOfBirth());
        profile.setGender(request.getGender());
        profile.setUpdatedAt(Timestamp.valueOf(LocalDateTime.now()));
        
        // Save profile first
        Profile savedProfile = profileRepository.save(profile);
        log.info("Profile saved successfully with ID: {}", savedProfile.getId());
        
        // Update user's updated_at timestamp
        user.setUpdatedAt(LocalDateTime.now());
        User savedUser = userRepository.save(user);
        log.info("User updated successfully with ID: {}", savedUser.getId());
        
        return savedUser;
    }

    @Override
    public void updatePassword(String username, UpdatePasswordRequest request) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updatePassword'");
    }

    @Override
    public void updateAvatar(String username, UpdateUserAvatarRequest request) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateAvatar'");
    }

    @Override
    public void updateEmail(String username, UpdateEmailRequest request) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateEmail'");
    }
    
}
