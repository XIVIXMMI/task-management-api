package com.omori.taskmanagement.service.task;

import com.omori.taskmanagement.dto.project.task.TaskCreateRequest;
import com.omori.taskmanagement.model.project.Task;

public interface TaskValidationService {

    void validateCreateTaskRequest(TaskCreateRequest request, Long userId);
    void validateTaskStatusUpdate(Task.TaskStatus currentStatus, Task.TaskStatus newStatus);
    void validateTaskProgress(Integer currentProgress, Integer newProgress, Task.TaskStatus status);
    void validateHierarchyRules(Long epicId, Long storyId);
    void validateTypeConversion(Task.TaskType currentType, Task.TaskType newType);
    // user
    void validateUserCanAccessTask(Long taskId, Long userId);
    void validateUserCanDeleteTask(Long taskId, Long userId);
    void validateUserCanEditTask(Long taskId, Long userId);
    }
