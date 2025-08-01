package com.omori.taskmanagement.springboot.service;

import com.omori.taskmanagement.springboot.dto.project.CreateTaskRequest;
import com.omori.taskmanagement.springboot.exceptions.TaskValidationException;
import com.omori.taskmanagement.springboot.model.project.Task;
import com.omori.taskmanagement.springboot.repository.project.CategoryRepository;
import com.omori.taskmanagement.springboot.repository.project.WorkspaceRepository;
import com.omori.taskmanagement.springboot.repository.usermgmt.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskValidationService {
    
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final WorkspaceRepository workspaceRepository;

    public void validateCreateTaskRequest(CreateTaskRequest request, Long userId) {
        log.debug("Validating create task request for user {}", userId);

        if( request == null ){
            throw new TaskValidationException("Request can not be null", Map.of("request", "Request can not be null"));
        }

        Map<String,String> errors = new HashMap<>();
        
        // Validate user exists
        if (!userRepository.existsById(userId)){
            errors.put("userId", "User not found");
        }

        // Validate date
        validateDates(request.getStartDate(), request.getDueDate(), errors);

         // Validate status and progress consistency
        validateStatusProgressConsistency(request.getStatus(), request.getProgress(), errors);

        // Validate category
        if( request.getCategoryId() != null && !categoryRepository.existsById(request.getCategoryId()) ) {
            errors.put("categoryId", "Category not found with id: " + request.getCategoryId());
        }

        // Validate assigned user
        if(request.getAssignedToId() != null && !userRepository.existsById(request.getAssignedToId()) ) {
            errors.put("assignedToId", "User not found with id: " + request.getAssignedToId());
        }

        // Validate workspace
        if(request.getWorkspaceId() != null && !workspaceRepository.existsById(request.getWorkspaceId()) ) {
            errors.put("workspaceId", "Workspace not found with id: " + request.getWorkspaceId());
        }

        // Validate progress
        if(request.getProgress() != null && (request.getProgress() < 0 || request.getProgress() > 100)){
            errors.put("progress", "Progress must be between 0 and 100");
        }

        // Validate estimate hours
        if(request.getEstimatedHours() != null && request.getEstimatedHours() < 0) {
            errors.put("estimatedHours", "Estimated hours must be greater than 0");
        }

        // Validate recurring pattern
        if(Boolean.TRUE.equals(request.getIsRecurring()) && (request.getRecurrencePattern() == null || request.getRecurrencePattern().isEmpty())) {
            errors.put("recurrencePattern","Recurrence pattern is required for reccurring tasks");
        } 
        
        if(!errors.isEmpty()){
            throw new TaskValidationException("Task validation failed", errors);
        }

        log.debug("Task validation successfull for user {}", userId);
    }

    public void validateTaskStatusUpdate(Task.TaskStatus currentStatus, Task.TaskStatus newStatus) {
        log.debug("Validating status update from {} to {}", currentStatus, newStatus);
        
        Map<String, String> errors = new HashMap<>();
        
        // Business rules for status transitions
        if (currentStatus == Task.TaskStatus.completed && newStatus == Task.TaskStatus.pending) {
            errors.put("status", "Cannot change completed task back to pending");
        }
        
        if (currentStatus == Task.TaskStatus.cancelled && newStatus != Task.TaskStatus.pending) {
            errors.put("status", "Cancelled tasks can only be changed to pending");
        }
        
        if (!errors.isEmpty()) {
            throw new TaskValidationException("Status update validation failed", errors);
        }
        
        log.debug("Status update validation passed");
    }

    public void validateTaskProgress(Integer currentProgress, Integer newProgress, Task.TaskStatus status) {
        log.debug("Validating progress update from {} to {} with status {}", currentProgress, newProgress, status);
        
        Map<String, String> errors = new HashMap<>();
        
        if (newProgress < 0 || newProgress > 100) {
            errors.put("progress", "Progress must be between 0 and 100");
        }
        
        if (status == Task.TaskStatus.completed && newProgress != 100) {
            errors.put("progress", "Completed tasks must have 100% progress");
        }
        
        if (status == Task.TaskStatus.pending && newProgress > 0) {
            errors.put("progress", "Pending tasks should have 0% progress");
        }
        
        if (!errors.isEmpty()) {
            throw new TaskValidationException("Progress update validation failed", errors);
        }
        
        log.debug("Progress update validation passed");
    }

    private void validateDates(LocalDateTime startDate, LocalDateTime dueDate, Map<String, String> errors) {
        LocalDateTime now = LocalDateTime.now();
        
        if (startDate != null && startDate.isBefore(now.minusDays(1))) {
            errors.put("startDate", "Start date cannot be more than 1 day in the past");
        }
        
        if (dueDate != null && dueDate.isBefore(now)) {
            errors.put("dueDate", "Due date cannot be in the past");
        }
        
        if (startDate != null && dueDate != null && startDate.isAfter(dueDate)) {
            errors.put("startDate", "Start date cannot be after due date");
        }
    }

    private void validateStatusProgressConsistency(Task.TaskStatus status, Integer progress, Map<String, String> errors) {
        if (status == Task.TaskStatus.completed && (progress == null || progress != 100)) {
            errors.put("progress", "Completed tasks must have 100% progress");
        }
        
        if (status == Task.TaskStatus.pending && progress != null && progress > 0) {
            errors.put("progress", "Pending tasks should have 0% progress");
        }
        
        if (status == Task.TaskStatus.in_progress && (progress == null || progress <= 0 || progress >= 100)) {
            errors.put("progress", "In-progress tasks must have progress between 1-99%");
        }
    }

}
