package com.omori.taskmanagement.service.task;

import com.omori.taskmanagement.dto.project.TaskFilterRequest;
import com.omori.taskmanagement.dto.project.TaskResponse;
import com.omori.taskmanagement.model.project.Task;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;

public interface TaskQueryService {

    Page<TaskResponse> findTasksByUserId(Long userId, TaskFilterRequest filter);
    Page<TaskResponse> getOverdueTasks(Long userId);
    Page<TaskResponse> searchTasks(Long userId, String keyword, TaskFilterRequest filter);
    Page<TaskResponse> getTasksByStatus(Long userId, Task.TaskStatus status);
    Page<TaskResponse> getTasksByPriority(Long userId, Task.TaskPriority priority);
    Page<TaskResponse> getTasksDueToday(Long userId);
    Page<TaskResponse> getTasksDueBetween(Long userId, LocalDateTime start, LocalDateTime end);
    Page<TaskResponse> getRecentlyUpdatedTasks(Long userId, int daysBack);
}
