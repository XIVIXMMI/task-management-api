package com.omori.taskmanagement.springboot.service;

import com.omori.taskmanagement.springboot.dto.usermgmt.UpdateUserAvatarRequest;
import com.omori.taskmanagement.springboot.dto.usermgmt.UpdateUserProfileRequest;
import com.omori.taskmanagement.springboot.model.usermgmt.User;
import com.omori.taskmanagement.springboot.security.dto.UpdateEmailRequest;
import com.omori.taskmanagement.springboot.security.dto.UpdatePasswordRequest;

public interface UserUpdateService {
    
    User updateProfile(String username, UpdateUserProfileRequest request);

    void updatePassword(String username, UpdatePasswordRequest request);

    void updateAvatar(String username, UpdateUserAvatarRequest request);   

    void updateEmail(String username, UpdateEmailRequest request);
}
