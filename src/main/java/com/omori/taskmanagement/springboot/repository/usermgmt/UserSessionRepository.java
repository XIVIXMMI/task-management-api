package com.omori.taskmanagement.springboot.repository.usermgmt;

import com.omori.taskmanagement.springboot.model.usermgmt.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserSessionRepository extends JpaRepository<Session, String> {
}
