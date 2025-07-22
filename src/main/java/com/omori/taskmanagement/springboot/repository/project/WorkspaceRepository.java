package com.omori.taskmanagement.springboot.repository.project;

import com.omori.taskmanagement.springboot.model.project.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkspaceRepository extends JpaRepository<Workspace, Long> {
}
