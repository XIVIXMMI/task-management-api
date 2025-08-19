package com.omori.taskmanagement.repository.audit;

import com.omori.taskmanagement.model.audit.ActionType;
import com.omori.taskmanagement.model.audit.ActivityLog;

import java.time.LocalDateTime;
import java.util.List;
import jakarta.annotation.Nonnull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {
    List<ActivityLog> findByUserId(@Nonnull Long userId);
    
    List<ActivityLog> findByTaskId(@Nonnull Long taskId);
    
    List<ActivityLog> findByWorkspaceId(@Nonnull Long workspaceId);
    
    List<ActivityLog> findByAction(@Nonnull ActionType action);
    
    List<ActivityLog> findByEntityType(@Nonnull String entityType);
    
    List<ActivityLog> findByCreatedAtBetween(@Nonnull LocalDateTime start, @Nonnull LocalDateTime end);
}
