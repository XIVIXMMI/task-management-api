package com.omori.taskmanagement.service.task.delete;

import com.omori.taskmanagement.model.project.Task;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.List;

public interface TaskDeletionService {

    /**
     * Soft deletes a single task (sets deletedAt timestamp).
     * Does not affect child tasks - they become orphaned.
     */
    void softDeleteTask(Long taskId, Long userId, Collection<? extends GrantedAuthority> authorities);

    /**
     * Soft deletes multiple tasks in batch.
     * Validates user permissions for each task.
     */
    void softDeleteMultipleTasks(List<Long> taskIds, Long userId, Collection<? extends GrantedAuthority> authorities);

    /**
     * Restores a soft-deleted task (clears deletedAt timestamp).
     */
    void restoreTask(Long taskId, Long userId, Collection<? extends GrantedAuthority> authorities);

    /**
     * Restores multiple soft-deleted tasks in batch.
     */
    void restoreMultipleTasks(List<Long> taskIds, Long userId, Collection<? extends GrantedAuthority> authorities);

    /**
     * Checks if user has permission to delete the specified task.
     * Users can delete if they are:
     * - The task owner
     * - Assigned to the task
     * - Have ROLE_ADMIN authority
     */
    boolean canDeleteTask(Task task, Long userId, Collection<? extends GrantedAuthority> authorities);
}