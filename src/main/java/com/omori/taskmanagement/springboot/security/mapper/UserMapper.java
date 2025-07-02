package com.omori.taskmanagement.springboot.security.mapper;

import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.omori.taskmanagement.springboot.model.usermgmt.User;
import com.omori.taskmanagement.springboot.security.dto.AuthenticatedUserDto;
import com.omori.taskmanagement.springboot.security.dto.RegistrationRequest;

import java.time.LocalDateTime;
import java.util.UUID;

@Mapper(componentModel = "spring", 
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        imports = {BCryptPasswordEncoder.class, PasswordEncoder.class, UUID.class, LocalDateTime.class})
public abstract class UserMapper {

    @Autowired
    protected PasswordEncoder passwordEncoder;

    @Mapping(target = "passwordHash", source = "password")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", expression = "java(UUID.randomUUID())")
    @Mapping(target = "emailVerifiedAt", ignore = true)
    @Mapping(target = "mobile", source = "mobile")
    @Mapping(target = "mobileVerifiedAt", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "roleId", expression = "java((short) 1)") // should fix this hard code 
    @Mapping(target = "profile", ignore = true)
    @Mapping(target = "isActive", expression = "java(true)")
    @Mapping(target = "isLocked", expression = "java(false)")
    @Mapping(target = "isVerified", expression = "java(false)")
    @Mapping(target = "createdAt", expression = "java(LocalDateTime.now())")
    @Mapping(target = "updatedAt", expression = "java(LocalDateTime.now())")
    
    public abstract User convertToUser(RegistrationRequest registrationRequest);

    public abstract AuthenticatedUserDto convertToAuthenticatedUserDto(User user);

    public abstract User convertToUser(AuthenticatedUserDto authenticatedUserDto);

    @AfterMapping
    protected void hashPassword(RegistrationRequest source, @MappingTarget User target) {
        if (source.getPassword() != null) {
            target.setPasswordHash(passwordEncoder.encode(source.getPassword()));
        }
    }
}
