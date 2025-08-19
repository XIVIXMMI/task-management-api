package com.omori.taskmanagement.repository.usermgmt;

import com.omori.taskmanagement.model.usermgmt.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserSessionRepository extends JpaRepository<Session, String> {
}
