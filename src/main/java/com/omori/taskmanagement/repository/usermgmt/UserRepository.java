package com.omori.taskmanagement.repository.usermgmt;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.omori.taskmanagement.model.usermgmt.User;

public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findByUsername(String username);

	boolean existsByEmail(String email);

	boolean existsByUsername(String username);

}
