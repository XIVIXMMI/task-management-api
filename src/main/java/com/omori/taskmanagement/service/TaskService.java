package com.omori.taskmanagement.service;

import com.omori.taskmanagement.dto.project.TaskCreateRequest;
import com.omori.taskmanagement.dto.project.TaskFilterRequest;
import com.omori.taskmanagement.dto.project.TaskResponse;
import com.omori.taskmanagement.dto.project.TaskUpdateRequest;
import com.omori.taskmanagement.springboot.dto.project.*;
import com.omori.taskmanagement.model.project.Task;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

public interface TaskService {
    // Create
    Task createTask(Long userId, TaskCreateRequest request);
    
    // Read
    TaskResponse getTaskById(Long taskId, Long userId);
    TaskResponse getTaskByUuid(UUID uuid, Long userId);
    Page<TaskResponse> getTasksByUser(Long userId, TaskFilterRequest filter);
    List<TaskResponse> getOverdueTasks(Long userId);
    
    // Update
    TaskResponse updateTask(Long taskId, Long userId, TaskUpdateRequest request);
    TaskResponse updateTaskStatus(Long taskId, Long userId, Task.TaskStatus status);
    TaskResponse updateTaskProgress(Long taskId, Long userId, Integer progress);
    
    // Delete
    void deleteTask(Long taskId, Long userId);
    void softDeleteTask(Long taskId, Long userId);
    
    // Batch operations
    List<TaskResponse> updateMultipleTasksStatus(List<Long> taskIds, Long userId, Task.TaskStatus status);
    void deleteMultipleTasks(List<Long> taskIds, Long userId);
    
    // Search
    Page<TaskResponse> searchTasks(Long userId, String keyword, TaskFilterRequest filter);

    // TODO: Add method get tasks with status, get tasks with an specific progress
}