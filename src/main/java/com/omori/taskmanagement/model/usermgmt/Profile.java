package com.omori.taskmanagement.model.usermgmt;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Type;

@Data
@NoArgsConstructor
@SuperBuilder
@Entity
@Table(name = "profiles", schema = "user_mgmt")
public class Profile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "profile_id")
    private Long id;

    @Column(nullable = false, name = "first_name", length = 50)
    @ColumnDefault("'guest'")
    private String firstName;

    @Column(name = "middle_name")
    private String middleName;

    @Column(nullable = false, name = "last_name")
    private String lastName;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "gender", length = 1)
    private String gender;

    @Column(name = "avatar_path")
    private String avatarPath;

    @Column(name = "timezone", length = 50)
    @ColumnDefault("'UTC'")
    private String timezone;

    @Column(name = "language", length = 5)
    @ColumnDefault("'en'")
    private String language;

    @Enumerated(EnumType.STRING)
    @Type(value = UserStatusType.class)
    @Column(name = "status", columnDefinition = "user_mgmt.user_status")
    private UserStatus status;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Column(name = "last_activity")
    private LocalDateTime lastActivity;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "auth_provider")
    private String authProvider;

    @Column(name = "auth_provider_id")
    private String authProviderId;

    @Column(name = "two_factor_enabled")
    private Boolean twoFactorEnabled;

    @Column(name = "session_id")
    private String sessionId;

    @Column(name = "login_attempts")
    private Short loginAttempts;

    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;

    @Column(name = "failed_login_at")
    private LocalDateTime failedLoginAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

}
