package com.omori.taskmanagement.service.task.delete;

import com.omori.taskmanagement.exceptions.task.TaskAccessDeniedException;
import com.omori.taskmanagement.exceptions.task.TaskNotFoundException;
import com.omori.taskmanagement.model.project.Task;
import com.omori.taskmanagement.repository.project.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class TaskDeletionServiceImpl implements TaskDeletionService {

    private final TaskRepository taskRepository;

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
    public void softDeleteTask(Long taskId, Long userId,
                                Collection<? extends GrantedAuthority> authorities) {
        log.debug("Attempting soft delete for taskId: {}, userId: {}", taskId, userId);

        validateTaskId(taskId);
        validateUserId(userId);

        Task task = taskRepository.findByIdAndDeletedAtIsNull(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with ID: " + taskId));

        if (task.getDeletedAt() != null) {
            throw new IllegalStateException(
                    String.format("Task %d is deleted", taskId)
            );
        }

        if(!canDeleteTask(task, userId, authorities)){
            throw new TaskAccessDeniedException(
                    String.format("User %d does not have permission to delete task %d", userId, taskId)
            );
        }

        task.setDeletedAt(LocalDateTime.now());
        taskRepository.save(task);
        log.debug("Task {} soft deleted by user {}", taskId, userId);
    }

    @Override
    @Transactional
    public void softDeleteMultipleTasks(List<Long> taskIds, Long userId,
                                        Collection<? extends GrantedAuthority> authorities) {
        log.debug("Attempting soft delete for taskIds: {}, userId: {}", taskIds, userId);

        // Input validation
        validateTaskIds(taskIds);
        validateUserId(userId);

        List<Task> tasks = taskRepository.findAllById(taskIds);

        List<Task> activeTasks = tasks.stream()
                .filter( t -> t.getDeletedAt() == null)
                .toList();

        List<Task> authorizedTasks = new ArrayList<>();
        List<Long> unauthorizedTaskIds = new ArrayList<>();

        for( Task task : activeTasks){
            if(canDeleteTask(task, userId, authorities)){
                authorizedTasks.add(task);
            } else {
                unauthorizedTaskIds.add(task.getId());
            }
        }

        if (!unauthorizedTaskIds.isEmpty()) {
            throw new TaskAccessDeniedException(
                    String.format("User %d lacks permission to delete tasks: %s",
                            userId, unauthorizedTaskIds)
            );
        }

        authorizedTasks.forEach( t -> t.setDeletedAt(LocalDateTime.now()));
        taskRepository.saveAll(authorizedTasks);

        log.debug("Soft deleted {} tasks by user {}", authorizedTasks.size(), userId);
    }

    @Override
    @Transactional
    public void restoreTask(Long taskId, Long userId,
                            Collection<? extends GrantedAuthority> authorities) {
        log.debug("Attempting restore for taskId: {}, userId: {}", taskId, userId);

        // Input validation
        validateTaskId(taskId);
        validateUserId(userId);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with ID: " + taskId));

        if(task.getDeletedAt() == null){
            throw new IllegalStateException(
                    String.format("Task %d is not deleted", taskId)
            );
        }

        if(!canDeleteTask(task, userId, authorities)){
            throw new TaskAccessDeniedException(
                    String.format("User %d does not have permission to restore task %d", userId, taskId)
            );
        }

        task.setDeletedAt(null);
        taskRepository.save(task);
        log.info("Restored task {} by user {}", taskId, userId);
    }

    @Override
    @Transactional
    public void restoreMultipleTasks(List<Long> taskIds, Long userId,
                                        Collection<? extends GrantedAuthority> authorities) {
        log.debug("Attempting restore for taskIds: {}, userId: {}", taskIds, userId);

        // Input validation
        validateTaskIds(taskIds);
        validateUserId(userId);

        List<Task> tasks = taskRepository.findAllById(taskIds);

        List<Task> activeTasks = tasks.stream()
                .filter( t -> t.getDeletedAt() != null)
                .toList();

        List<Task> authorizedTasks = new ArrayList<>();
        List<Task> unauthorizedTasks = new ArrayList<>();

        for( Task task : activeTasks){
            if(canDeleteTask(task, userId, authorities)){
                authorizedTasks.add(task);
            } else {
                unauthorizedTasks.add(task);
            }
        }

        if (!unauthorizedTasks.isEmpty()) {
            throw new TaskAccessDeniedException(
                    String.format("User %d lacks permission to delete tasks: %s",
                            userId, unauthorizedTasks)
            );
        }

        authorizedTasks.forEach( t -> t.setDeletedAt(null));
        taskRepository.saveAll(authorizedTasks);

        log.debug("Restore {} tasks by user {}", authorizedTasks.size(), userId);
    }

    @Override
    public boolean canDeleteTask(Task task, Long userId,
                                    Collection<? extends GrantedAuthority> authorities) {
        // Check if user has ROLE_ADMIN
        boolean isAdmin = authorities.stream()
                .anyMatch(auth -> "ROLE_ADMIN".equals(auth.getAuthority()));

        if (isAdmin) {
            return true;
        }

        // Check if user is owner or assignee
        boolean isOwner = task.getUser() != null &&
                task.getUser().getId().equals(userId);
        boolean isAssignee = task.getAssignedTo() != null &&
                task.getAssignedTo().getId().equals(userId);
        return isOwner || isAssignee;
    }
}
