package com.omori.taskmanagement.service;

import com.omori.taskmanagement.dto.usermgmt.UpdateUserAvatarRequest;
import com.omori.taskmanagement.dto.usermgmt.UpdateUserProfileRequest;
import com.omori.taskmanagement.model.usermgmt.User;
import com.omori.taskmanagement.dto.usermgmt.UpdateEmailRequest;
import com.omori.taskmanagement.dto.usermgmt.UpdatePasswordRequest;

public interface UserUpdateService {
    
    User updateProfile(String username, UpdateUserProfileRequest request);

    void updatePassword(String username, UpdatePasswordRequest request);

    void updateAvatar(String username, UpdateUserAvatarRequest request);   

    void updateEmail(String username, UpdateEmailRequest request);
}
