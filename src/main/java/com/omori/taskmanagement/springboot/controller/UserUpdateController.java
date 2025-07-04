package com.omori.taskmanagement.springboot.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.omori.taskmanagement.springboot.dto.usermgmt.UpdateUserProfileRequest;
import com.omori.taskmanagement.springboot.exceptions.UserNotFoundException;
import com.omori.taskmanagement.springboot.model.usermgmt.User;
import com.omori.taskmanagement.springboot.dto.usermgmt.UpdateUserProfileResponse;
import com.omori.taskmanagement.springboot.service.UserUpdateService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/user")
@Slf4j
@Tag(name = "User Update", description = "User Update API")
public class UserUpdateController {

    private final UserUpdateService userUpdateService;

    @PatchMapping("/update-profile/{username}")
    @PreAuthorize("@authService.hasPermission(#username)")
    @Operation(summary = "Update User Profile", description = "Update user profile")
    public ResponseEntity<UpdateUserProfileResponse> updateProfile(
            @PathVariable String username,
            @Valid @RequestBody UpdateUserProfileRequest request) {
        try {
            userUpdateService.updateProfile(username, request);
            UpdateUserProfileResponse response = new UpdateUserProfileResponse(
                "User profile updated successfully",
                request.getFirstName(),
                request.getLastName(),
                request.getMiddleName(),
                request.getDateOfBirth(),
                request.getGender()
            );
            return ResponseEntity.ok(response);
        } catch (UserNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Failed to update profile for user: {}", username, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
