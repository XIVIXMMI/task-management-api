package com.omori.taskmanagement.springboot.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

@Data
@Entity
@Table(name = "user_profiles")
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;
    private String middleName;
    private String lastName;
    private LocalDate dateOfBirth;
    private String avatarPath;

    @Enumerated(EnumType.STRING)
    private UserStatus status = UserStatus.OFFLINE;

    private LocalDateTime lastLogin;
    private LocalDateTime lastActivity;
    private LocalDateTime verifiedAt;
    private LocalDateTime deletedAt;

    private String authProvider;
    private String authProviderId;
    private Boolean twoFactorEnabled = false;

    private String sessionId;
    private Short loginAttempt = 0;
    private LocalDateTime failLoginAt;

    // Getters, setters, constructors...
}
