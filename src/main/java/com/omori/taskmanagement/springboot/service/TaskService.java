package com.omori.taskmanagement.springboot.service;

import com.omori.taskmanagement.springboot.dto.project.*;
import com.omori.taskmanagement.springboot.model.project.Task;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

public interface TaskService {
    // Create
    Task createTask(Long userId, CreateTaskRequest request);
    
    // Read
    GetTaskResponse getTaskById(Long taskId, Long userId);
    GetTaskResponse getTaskByUuid(UUID uuid, Long userId);
    Page<GetTaskResponse> getTasksByUser(Long userId, TaskFilterRequest filter);
    List<GetTaskResponse> getOverdueTasks(Long userId);
    
    // Update
    GetTaskResponse updateTask(Long taskId, Long userId, UpdateTaskRequest request);
    GetTaskResponse updateTaskStatus(Long taskId, Long userId, Task.TaskStatus status);
    GetTaskResponse updateTaskProgress(Long taskId, Long userId, Integer progress);
    
    // Delete
    void deleteTask(Long taskId, Long userId);
    void softDeleteTask(Long taskId, Long userId);
    
    // Batch operations
    List<GetTaskResponse> updateMultipleTasksStatus(List<Long> taskIds, Long userId, Task.TaskStatus status);
    void deleteMultipleTasks(List<Long> taskIds, Long userId);
    
    // Search
    Page<GetTaskResponse> searchTasks(Long userId, String keyword, TaskFilterRequest filter);

    // TODO: Add method get tasks with status, get tasks with an specific progress
}