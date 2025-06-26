package com.omori.taskmanagement.springboot.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.omori.taskmanagement.springboot.model.UserRole;

public interface UserRoleRepository  extends JpaRepository<UserRole, Short>{

    Optional<UserRole> findByName(String name);

}