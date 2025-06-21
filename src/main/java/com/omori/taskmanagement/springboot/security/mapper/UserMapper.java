package com.omori.taskmanagement.springboot.security.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import com.omori.taskmanagement.springboot.model.User;
import com.omori.taskmanagement.springboot.security.dto.AuthenticatedUserDto;
import com.omori.taskmanagement.springboot.security.dto.RegistrationRequest;


@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

	UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

	User convertToUser(RegistrationRequest registrationRequest);

	AuthenticatedUserDto convertToAuthenticatedUserDto(User user);

	User convertToUser(AuthenticatedUserDto authenticatedUserDto);

}
