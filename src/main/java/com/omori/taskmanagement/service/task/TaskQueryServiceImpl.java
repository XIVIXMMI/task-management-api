package com.omori.taskmanagement.service.task;

import com.omori.taskmanagement.dto.project.task.TaskFilterRequest;
import com.omori.taskmanagement.dto.project.task.TaskResponse;
import com.omori.taskmanagement.model.project.Task;
import com.omori.taskmanagement.repository.project.TaskRepository;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class TaskQueryServiceImpl implements TaskQueryService{

    private final TaskRepository taskRepository; // TODO: use in real implementation
    @Override
    public Page<TaskResponse> findTasksByUserId(Long userId, TaskFilterRequest filter) {
        log.warn("findTasksByUserId not implemented; returning empty page for userId={}", userId);
        return Page.empty();
    }

    @Override
    public Page<TaskResponse> getOverdueTasks(Long userId) {
        log.warn("getOverdueTasks not implemented; returning empty page for userId={}", userId);
        return Page.empty();
    }

    @Override
    public Page<TaskResponse> searchTasks(Long userId, String keyword, TaskFilterRequest filter) {
        log.warn("searchTasks not implemented; returning empty page for userId={}, keyword='{}'", userId, keyword);
        return Page.empty();
    }

    @Override
    public Page<TaskResponse> getTasksByStatus(Long userId, Task.TaskStatus status) {
        log.warn("getTasksByStatus not implemented; returning empty page for userId={}, status={}", userId, status);
        return Page.empty();
    }

    @Override
    public Page<TaskResponse> getTasksByPriority(Long userId, Task.TaskPriority priority) {
        log.warn("getTasksByPriority not implemented; returning empty page for userId={}, priority={}", userId, priority);
        return Page.empty();
    }

    @Override
    public Page<TaskResponse> getTasksDueToday(Long userId) {
        log.warn("getTasksDueToday not implemented; returning empty page for userId={}", userId);
        return Page.empty();
    }

    @Override
    public Page<TaskResponse> getTasksDueBetween(Long userId, LocalDateTime start, LocalDateTime end) {
        log.warn("getTasksDueBetween not implemented; returning empty page for userId={}, start={}, end={}", userId, start, end);
        return Page.empty();
    }

    @Override
    public Page<TaskResponse> getRecentlyUpdatedTasks(Long userId, int daysBack) {
        log.warn("getRecentlyUpdatedTasks not implemented; returning empty page for userId={}, daysBack={}", userId, daysBack);
        return Page.empty();
    }
}
