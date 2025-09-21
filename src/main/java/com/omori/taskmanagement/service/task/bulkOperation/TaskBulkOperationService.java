package com.omori.taskmanagement.service.task.bulkOperation;

import com.omori.taskmanagement.dto.project.task.TaskResponse;
import com.omori.taskmanagement.model.project.Task;

import java.util.List;

public interface TaskBulkOperationService {
    // Status operations
    List<TaskResponse> updateMultipleTasksStatus(List<Long> taskIds, Long userId, Task.TaskStatus status);
    List<TaskResponse> updateMultipleTasksProgress(List<Long> taskIds, Long userId, Integer progress);

    // Assignment operations
    List<TaskResponse> assignMultipleTasks(List<Long> taskIds, Long userId, Long assigneeId);
    List<TaskResponse> updateMultipleTasksPriority(List<Long> taskIds, Long userId, Task.TaskPriority priority);

    // Deletion operations
    void softDeleteMultipleTasks(List<Long> taskIds, Long userId);
    void archiveMultipleTasks(List<Long> taskIds, Long userId);

    // Hierarchy operations
    void moveMultipleTasksToParent(List<Long> taskIds, Long newParentId, Long userId);

    // Type conversion operations (for your conversion feature)
    List<TaskResponse> convertMultipleTasksType(List<Long> taskIds, Task.TaskType targetType, Long userId);
}
