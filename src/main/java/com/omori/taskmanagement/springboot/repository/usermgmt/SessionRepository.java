package com.omori.taskmanagement.springboot.repository.usermgmt;

import com.omori.taskmanagement.springboot.model.usermgmt.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SessionRepository extends JpaRepository<Session, String> {
}
