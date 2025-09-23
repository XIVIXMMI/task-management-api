package com.omori.taskmanagement.service.task.delete;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class TaskDeletionServiceImpl implements TaskDeletionService {

    /**
     * Validates a single taskId parameter.
     *
     * @param taskId the task ID to validate
     * @throws IllegalArgumentException if taskId is null
     */
    private void validateTaskId(Long taskId) {
        if (taskId == null) {
            throw new IllegalArgumentException("Task ID cannot be null");
        }
    }

    /**
     * Validates userId parameter.
     *
     * @param userId the user ID to validate
     * @throws IllegalArgumentException if userId is null
     */
    private void validateUserId(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
    }

    /**
     * Validates taskIds list parameter.
     *
     * @param taskIds the list of task IDs to validate
     * @throws IllegalArgumentException if taskIds is null, empty, or contains null values
     */
    private void validateTaskIds(List<Long> taskIds) {
        if (taskIds == null) {
            throw new IllegalArgumentException("Task IDs list cannot be null");
        }
        if (taskIds.isEmpty()) {
            throw new IllegalArgumentException("Task IDs list cannot be empty");
        }
        if (taskIds.contains(null)) {
            throw new IllegalArgumentException("Task IDs list cannot contain null values");
        }
    }

    @Override
    @Transactional
    public void softDeleteTask(Long taskId, Long userId) {
        log.debug("Attempting soft delete for taskId: {}, userId: {}", taskId, userId);

        // Input validation
        validateTaskId(taskId);
        validateUserId(userId);

        // Fail fast - method not implemented yet
        throw new UnsupportedOperationException(
                String.format("softDeleteTask(taskId=%d, userId=%d) is not implemented yet. " +
                        "This method should set the deletedAt timestamp for the specified task.",
                        taskId, userId)
        );
    }

    @Override
    @Transactional
    public void softDeleteMultipleTasks(List<Long> taskIds, Long userId) {
        log.debug("Attempting soft delete for taskIds: {}, userId: {}", taskIds, userId);

        // Input validation
        validateTaskIds(taskIds);
        validateUserId(userId);

        // Fail fast - method not implemented yet
        throw new UnsupportedOperationException(
                String.format("softDeleteMultipleTasks(taskIds=%s, userId=%d) is not implemented yet. " +
                        "This method should batch soft delete %d tasks by setting their deletedAt timestamps.",
                        taskIds, userId, taskIds.size())
        );
    }

    @Override
    @Transactional
    public void restoreTask(Long taskId, Long userId) {
        log.debug("Attempting restore for taskId: {}, userId: {}", taskId, userId);

        // Input validation
        validateTaskId(taskId);
        validateUserId(userId);

        // Fail fast - method not implemented yet
        throw new UnsupportedOperationException(
                String.format("restoreTask(taskId=%d, userId=%d) is not implemented yet. " +
                        "This method should clear the deletedAt timestamp for the specified task.",
                        taskId, userId)
        );
    }

    @Override
    @Transactional
    public void restoreMultipleTasks(List<Long> taskIds, Long userId) {
        log.debug("Attempting restore for taskIds: {}, userId: {}", taskIds, userId);

        // Input validation
        validateTaskIds(taskIds);
        validateUserId(userId);

        // Fail fast - method not implemented yet
        throw new UnsupportedOperationException(
                String.format("restoreMultipleTasks(taskIds=%s, userId=%d) is not implemented yet. " +
                        "This method should batch restore %d tasks by clearing their deletedAt timestamps.",
                        taskIds, userId, taskIds.size())
        );
    }

    @Override
    public boolean canDeleteTask(Long taskId, Long userId) {
        log.debug("Checking delete permission for taskId: {}, userId: {}", taskId, userId);

        // Input validation
        validateTaskId(taskId);
        validateUserId(userId);

        // Fail fast - method not implemented yet
        throw new UnsupportedOperationException(
                String.format("canDeleteTask(taskId=%d, userId=%d) is not implemented yet. " +
                        "This method should check if the user has permission to delete the specified task.",
                        taskId, userId)
        );
    }
}
