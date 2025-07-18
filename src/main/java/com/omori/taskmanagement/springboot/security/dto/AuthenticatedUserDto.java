package com.omori.taskmanagement.springboot.security.dto;

import com.omori.taskmanagement.springboot.model.usermgmt.Role;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AuthenticatedUserDto {


	private String username;


	private Role role;

}
