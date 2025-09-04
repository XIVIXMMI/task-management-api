package com.omori.taskmanagement.service.task;

import com.omori.taskmanagement.dto.project.task.TaskFilterRequest;
import com.omori.taskmanagement.dto.project.task.TaskResponse;
import com.omori.taskmanagement.model.project.Task;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;

public class TaskQueryServiceImpl implements TaskQueryService{
    @Override
    public Page<TaskResponse> findTasksByUserId(Long userId, TaskFilterRequest filter) {
        return null;
    }

    @Override
    public Page<TaskResponse> getOverdueTasks(Long userId) {
        return null;
    }

    @Override
    public Page<TaskResponse> searchTasks(Long userId, String keyword, TaskFilterRequest filter) {
        return null;
    }

    @Override
    public Page<TaskResponse> getTasksByStatus(Long userId, Task.TaskStatus status) {
        return null;
    }

    @Override
    public Page<TaskResponse> getTasksByPriority(Long userId, Task.TaskPriority priority) {
        return null;
    }

    @Override
    public Page<TaskResponse> getTasksDueToday(Long userId) {
        return null;
    }

    @Override
    public Page<TaskResponse> getTasksDueBetween(Long userId, LocalDateTime start, LocalDateTime end) {
        return null;
    }

    @Override
    public Page<TaskResponse> getRecentlyUpdatedTasks(Long userId, int daysBack) {
        return null;
    }
}
