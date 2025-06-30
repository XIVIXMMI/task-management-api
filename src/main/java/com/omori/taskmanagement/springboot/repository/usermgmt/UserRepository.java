package com.omori.taskmanagement.springboot.repository.usermgmt;

import org.springframework.data.jpa.repository.JpaRepository;

import com.omori.taskmanagement.springboot.model.usermgmt.User;

public interface UserRepository extends JpaRepository<User, Long> {

	User findByUsername(String username);

	boolean existsByEmail(String email);

	boolean existsByUsername(String username);

}
