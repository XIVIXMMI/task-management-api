package com.omori.taskmanagement.service.task.utils;

import com.omori.taskmanagement.dto.project.task.creation.BaseTaskCreateRequest;
import com.omori.taskmanagement.exceptions.task.TaskValidationException;
import com.omori.taskmanagement.model.project.Task;

public interface TaskValidationService {

    void validateCreateTaskRequest(BaseTaskCreateRequest request, Long userId);
    void validateTaskStatusUpdate(Task.TaskStatus currentStatus, Task.TaskStatus newStatus);
    void validateTaskProgress(Integer currentProgress, Integer newProgress, Task.TaskStatus status);
    void validateHierarchyRules(Long epicId, Long storyId);
    void validateTypeConversion(Task.TaskType currentType, Task.TaskType newType);
    // user
    void validateUserCanAccessTask(Long taskId, Long userId);
    void validateUserCanDeleteTask(Long taskId, Long userId);
    void validateUserCanEditTask(Long taskId, Long userId);

    /**
     * Validates that parentId is not provided for standalone task creation
     * @param request the task creation request to validate
     * @param taskType the type of task being created (for error messaging)
     * @throws TaskValidationException if parentId is provided
     */
    void validateNoParentIdAllowed(BaseTaskCreateRequest request, String taskType);
    }
