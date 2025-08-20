package com.omori.taskmanagement.repository.project;

import com.omori.taskmanagement.model.project.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkspaceRepository extends JpaRepository<Workspace, Long> {
}
