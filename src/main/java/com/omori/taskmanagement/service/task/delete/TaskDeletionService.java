package com.omori.taskmanagement.service.task.delete;

import java.util.List;

public interface TaskDeletionService {
    // Soft delete with cascading
    void softDeleteTaskWithChildren(Long taskId, Long userId);
    void softDeleteMultipleTasksWithChildren(List<Long> taskIds, Long userId);

    // Archive with cascading
    // Need to define new field in model like: isArchive
    void archiveTaskWithChildren(Long taskId, Long userId);
    void archiveMultipleTasksWithChildren(List<Long> taskIds, Long userId);

    // Restore operations
    void restoreTaskWithChildren(Long taskId, Long userId);

    // Hard delete (admin only, permanent)
    void permanentlyDeleteTask(Long taskId, Long userId);

    // Utility methods
    List<Long> getAllChildTaskIds(Long parentTaskId);
    boolean canDeleteTask(Long taskId, Long userId);
}
