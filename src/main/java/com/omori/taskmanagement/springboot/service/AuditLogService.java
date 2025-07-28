package com.omori.taskmanagement.springboot.service;

import com.omori.taskmanagement.springboot.dto.common.RequestMetadata;
import com.omori.taskmanagement.springboot.model.audit.ActionType;
import com.omori.taskmanagement.springboot.model.audit.ActivityLog;
import com.omori.taskmanagement.springboot.model.project.Task;
import com.omori.taskmanagement.springboot.model.project.Workspace;
import com.omori.taskmanagement.springboot.model.usermgmt.User;
import com.omori.taskmanagement.springboot.repository.audit.ActivityLogRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogService {

    private final ActivityLogRepository activityLogRepository;
    
    public void loggingActivity(
            ActionType actionType,
            String entityType,
            Long entityId,
            User user,
            Task task,
            Workspace workspace,
            Map<String, Object> oldValue,
            Map<String, Object> newValue,
            RequestMetadata metadata
            ){
        try {
            String userAgent = metadata.getUserAgent();
            String ipAddress = metadata.getIpAddress();

            ActivityLog logging = ActivityLog.builder()
                    .action(actionType)
                    .entityType(entityType)
                    .entityId(entityId)
                    .user(user)
                    .task(task)
                    .workspace(workspace)
                    .newValues(newValue)
                    .oldValues(oldValue)
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .createdAt(LocalDateTime.now())
                    .build();
            // Save the activity log to the repository
            activityLogRepository.save(logging);
            
        } catch (Exception e) {
            log.error("Error logging activity: {}", e.getMessage(), e);
            // Optionally, you can handle the exception further or rethrow it
            throw new RuntimeException("Failed to log activity", e);
        }
    }

    public void computeDiff ( Map<String, Object> oldValue,
            Map<String, Object> newValue) {
        // Implement logic to compute the difference between oldValue and newValue
    }


}
