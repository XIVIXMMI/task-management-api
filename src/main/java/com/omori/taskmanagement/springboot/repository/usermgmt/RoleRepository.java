package com.omori.taskmanagement.springboot.repository.usermgmt;

import com.omori.taskmanagement.springboot.model.usermgmt.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Short> {
    Optional<Role> findById(Short id);
    Optional<Role> findByName(String name);
    Optional<Role> findByNameIgnoreCase(String name);
    boolean existsByName(String name);
    boolean existsByNameIgnoreCase(String name);
}
