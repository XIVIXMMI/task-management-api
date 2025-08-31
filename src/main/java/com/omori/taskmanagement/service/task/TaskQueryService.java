package com.omori.taskmanagement.service.task;

import com.omori.taskmanagement.dto.project.TaskFilterRequest;
import com.omori.taskmanagement.dto.project.TaskResponse;
import com.omori.taskmanagement.model.project.Task;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;

public interface TaskQueryService {

    Page<TaskResponse> findTasksByUserId(Long userId, TaskFilterRequest filter);
    List<TaskResponse> getOverdueTasks(Long userId);
    Page<TaskResponse> searchTasks(Long userId, String keyword, TaskFilterRequest filter);
    Page<TaskResponse> getTasksByStatus(Long userId, Task.TaskStatus status);
    List<TaskResponse> getTasksByPriority(Long userId, Task.TaskPriority priority);
    List<TaskResponse> getTasksDueToday(Long userId);
    List<TaskResponse> getTasksDueBetween(Long userId, LocalDateTime start, LocalDateTime end);
    List<TaskResponse> getRecentlyUpdatedTasks(Long userId, int daysBack);
}
