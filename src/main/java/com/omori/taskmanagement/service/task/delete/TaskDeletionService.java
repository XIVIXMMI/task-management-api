package com.omori.taskmanagement.service.task.delete;

import com.omori.taskmanagement.model.project.Task;

import java.util.List;

public interface TaskDeletionService {

    /**
     * Soft deletes a single task (sets deletedAt timestamp).
     * Does not affect child tasks - they become orphaned.
     */
    void softDeleteTask(Long taskId, Long userId);

    /**
     * Soft deletes multiple tasks in batch.
     * Validates user permissions for each task.
     */
    void softDeleteMultipleTasks(List<Long> taskIds, Long userId);

    /**
     * Restores a soft-deleted task (clears deletedAt timestamp).
     */
    void restoreTask(Long taskId, Long userId);

    /**
     * Restores multiple soft-deleted tasks in batch.
     */
    void restoreMultipleTasks(List<Long> taskIds, Long userId);

    /**
     * Checks if user has permission to delete the specified task.
     */
    boolean canDeleteTask(Task task, Long userId);
}