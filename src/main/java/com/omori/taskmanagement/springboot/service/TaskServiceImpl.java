package com.omori.taskmanagement.springboot.service;

import com.omori.taskmanagement.springboot.dto.project.CreateTaskRequest;
import com.omori.taskmanagement.springboot.dto.project.GetTaskResponse;
import com.omori.taskmanagement.springboot.dto.project.TaskFilterRequest;
import com.omori.taskmanagement.springboot.dto.project.UpdateTaskRequest;
import com.omori.taskmanagement.springboot.exceptions.TaskAccessDeniedException;
import com.omori.taskmanagement.springboot.exceptions.TaskBusinessException;
import com.omori.taskmanagement.springboot.exceptions.TaskNotFoundException;
import com.omori.taskmanagement.springboot.exceptions.TaskValidationException;
import com.omori.taskmanagement.springboot.exceptions.UserNotFoundException;
import com.omori.taskmanagement.springboot.exceptions.WorkspaceNotFoundException;
import com.omori.taskmanagement.springboot.model.project.Category;
import com.omori.taskmanagement.springboot.model.project.Task;
import com.omori.taskmanagement.springboot.model.project.Workspace;
import com.omori.taskmanagement.springboot.model.usermgmt.User;
import com.omori.taskmanagement.springboot.repository.project.CategoryRepository;
import com.omori.taskmanagement.springboot.repository.project.TaskRepository;
import com.omori.taskmanagement.springboot.repository.project.WorkspaceRepository;
import com.omori.taskmanagement.springboot.repository.usermgmt.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskServiceImpl implements TaskService {

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final CategoryRepository categoryRepository;
    private final WorkspaceRepository workspaceRepository;
    // inject validate service
    private final TaskValidationService taskValidationService;

    @Override
    @Transactional
    @CacheEvict(value = {"tasks", "taskDetails"}, allEntries = true)
    public Task createTask(Long id, CreateTaskRequest request) {
        log.info("Creating task for user with id: {}", id);

        // Validate request
        taskValidationService.validateCreateTaskRequest(request, id);

        try {

            User user = userRepository
                    .findById(id)
                    .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));

            Task task = Task.builder()
                    .title(request.getTitle())
                    .description(request.getDescription())
                    .dueDate(request.getDueDate())
                    .startDate(request.getStartDate())
                    .priority(request.getPriority())
                    .status(Task.TaskStatus.pending)
                    .estimatedHours(request.getEstimatedHours())
                    .actualHours(0.0)
                    .progress(0)
                    .user(user)
                    .sortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0)
                    .isRecurring(request.getIsRecurring() != null ? request.getIsRecurring() : false)
                    .recurrencePattern(request.getRecurrencePattern())
                    .metadata(request.getMetadata())
                    .build();

            // Set relate with validation
            setTaskRelations(task,
                    request.getCategory() != null ? request.getCategory().getId() : null,
                    request.getAssignedTo() != null ? request.getAssignedTo().getId() : null,
                    request.getWorkspace() != null ? request.getWorkspace().getId() : null);

            Task savedTask = taskRepository.save(task);
            log.info("Task created successfully with id: {}", savedTask.getId());
            return savedTask;
        } catch (Exception e) {
            log.error("Error creating task for user id {} ", id, e);
            throw new TaskBusinessException("Failed to create task: " + e.getMessage(), e);
        }

    }

    // Helper method to set task relations
    private void setTaskRelations(Task task, Long categoryId, Long assignedToId, Long workspaceId) {
        if (categoryId != null) {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new TaskBusinessException("Category not found with id: " + categoryId));
            task.setCategory(category);
        }

        if (assignedToId != null) {
            User assignedTo = userRepository.findById(assignedToId)
                    .orElseThrow(() -> new UserNotFoundException("User not found with id: " + assignedToId));
            task.setAssignedTo(assignedTo);
        }

        if (workspaceId != null) {
            Workspace workspace = workspaceRepository.findById(workspaceId)
                    .orElseThrow(() -> new TaskBusinessException("Workspace not found with id: " + workspaceId));
            task.setWorkspace(workspace);
        }
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "taskDetails", key = "'task:' + #taskId")
    //NOTE: could separate for user's owner or user assigned
    public GetTaskResponse getTaskById(Long taskId, Long userId) {
        log.info("Getting task with id: {} for user {}", taskId, userId);

        Task task = taskRepository.findByIdWithRelations(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + taskId));

        validateTaskAccess(task, userId);

        return GetTaskResponse.from(task);
    }

    // NOTE: Why we need to pass the userId to get uuid ??
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "taskDetails", key = "#uuid.toString() + '_' + #userId")
    public GetTaskResponse getTaskByUuid(UUID uuid, Long userId) {
        log.info("Getting task with uuid: {} for user {}", uuid, userId);

        Task task = taskRepository.findByUuid(uuid)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with uuid: " + uuid));
        validateTaskAccess(task, userId);
        return GetTaskResponse.from(task);
    }

    @Override   
    @Transactional(readOnly = true)
    @Cacheable(value = "tasks", key = "#userId + '_' + T(java.util.Objects).hash(#filter)")
    public Page<GetTaskResponse> getTasksByUser(Long userId, TaskFilterRequest filter) {
        log.info("Getting tasks for user with id: {}", userId);
        Pageable pageable = createPageable(filter);
        Page<Task> tasks = taskRepository.findByUserIdAndNotDeleted(userId, pageable);
        return tasks.map(GetTaskResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GetTaskResponse> getOverdueTasks(Long userId) {
        log.info("Getting overdue tasks for user with id: {}", userId);
        List<Task> overdueTasks = taskRepository.findOverdueTasksByUserId(userId, LocalDateTime.now());

        return overdueTasks.stream()
                .map(GetTaskResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @CacheEvict(value = {"tasks", "taskDetails"}, key = "#taskId + '_' + #userId")
    @CachePut(value = "taskDetails", key = "#taskId + '_' + #userId")
    public GetTaskResponse updateTask(Long taskId, Long userId, UpdateTaskRequest request) {
        log.info("Updating task with id: {} for user {}", taskId, userId);

        Task task = taskRepository.findByIdWithRelations(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + taskId));
        validateTaskAccess(task, userId);

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setDueDate(request.getDueDate());
        task.setStartDate(request.getStartDate());
        task.setPriority(request.getPriority());
        task.setStatus(request.getStatus());
        task.setEstimatedHours(request.getEstimatedHours());
        task.setActualHours(request.getActualHours());
        task.setProgress(request.getProgress());
        task.setSortOrder(request.getSortOrder());
        task.setIsRecurring(request.getIsRecurring());
        task.setRecurrencePattern(request.getRecurrencePattern());
        task.setMetadata(request.getMetadata());

        // Update category if provided
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new TaskBusinessException("Category not found with id: " + request.getCategoryId()));
            task.setCategory(category);
        }

        // Update assigned user if provided
        if (request.getAssignedToId() != null) {
            User assignedTo = userRepository.findById(request.getAssignedToId())
                    .orElseThrow(() -> new UserNotFoundException(
                            "Assigned user not found with id: " + request.getAssignedToId()));
            task.setAssignedTo(assignedTo);
        }

        // Update workspace if provided
        if (request.getWorkspaceId() != null) {
            Workspace workspace = workspaceRepository.findById(request.getWorkspaceId())
                    .orElseThrow(() -> new WorkspaceNotFoundException(
                            "Workspace not found with id: " + request.getWorkspaceId()));
            task.setWorkspace(workspace);
        }

        // Auto-update completion date
        if (request.getStatus() == Task.TaskStatus.completed && task.getCompletedAt() == null) {
            task.setCompletedAt(LocalDateTime.now());
        } else if (request.getStatus() != Task.TaskStatus.completed) {
            task.setCompletedAt(null);
        }

        Task updatedTask = taskRepository.save(task);
        log.info("Task updated successfully: {}", taskId);

        return GetTaskResponse.from(updatedTask);

    }

    @Override
    @Transactional
    @CacheEvict(value = {"tasks", "taskDetails"}, key = "#taskId + '_' + #userId")
    @CachePut(value = "taskDetails", key = "#taskId + '_' + #userId")
    public GetTaskResponse updateTaskStatus(Long taskId, Long userId, Task.TaskStatus status) {
        log.info("Updating task status: {} to {} for user: {}", taskId, status, userId);

        try {
            Task task = taskRepository.findByIdWithRelations(taskId)
                    .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + taskId));

            validateTaskAccess(task, userId);

            // Validate status transition
            taskValidationService.validateTaskStatusUpdate(task.getStatus(), status);

            task.setStatus(status);

            // Auto-update completion date and progress
            if (status == Task.TaskStatus.completed) {
                task.setCompletedAt(LocalDateTime.now());
                task.setProgress(100);
            } else if (status == Task.TaskStatus.in_progress && task.getProgress() == 0) {
                task.setProgress(10);
            } else if (status != Task.TaskStatus.completed) {
                task.setCompletedAt(null);
            }

            Task updatedTask = taskRepository.save(task);
            log.info("Task status updated successfully: {} to {} for user: {}", taskId, status, userId);

            return GetTaskResponse.from(updatedTask);

        } catch (TaskNotFoundException | TaskAccessDeniedException | TaskValidationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error updating task status for task: {} and user: {}", taskId, userId, e);
            throw new TaskBusinessException("Failed to update task status: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = {"tasks", "taskDetails"}, key = "#taskId + '_' + #userId")
    @CachePut(value = "taskDetails", key = "#taskId + '_' + #userId")
    public GetTaskResponse updateTaskProgress(Long taskId, Long userId, Integer progress) {
        log.info("Updating task progress: {} to {}% for user: {}", taskId, progress, userId);

        Task task = taskRepository.findByIdWithRelations(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + taskId));

        validateTaskAccess(task, userId);

        task.setProgress(progress);

        // Auto-update status based on progress
        if (progress == 0) {
            task.setStatus(Task.TaskStatus.pending);
        } else if (progress > 0 && progress < 100) {
            task.setStatus(Task.TaskStatus.in_progress);
        } else if (progress == 100) {
            task.setStatus(Task.TaskStatus.completed);
            task.setCompletedAt(LocalDateTime.now());
        }

        Task updatedTask = taskRepository.save(task);
        log.info("Task progress updated successfully: {}", taskId);

        return GetTaskResponse.from(updatedTask);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"tasks", "taskDetails"}, key = "#taskId + '_' + #userId")
    public void deleteTask(Long taskId, Long userId) {
        log.info("Permanently deleting task: {} for user: {}", taskId, userId);

        Task task = taskRepository.findByIdWithRelations(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + taskId));

        validateTaskAccess(task, userId);

        taskRepository.delete(task);
        log.info("Task deleted permanently: {}", taskId);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"tasks", "taskDetails"}, key = "#taskId + '_' + #userId")
    public void softDeleteTask(Long taskId, Long userId) {
        log.info("Soft deleting task: {} for user: {}", taskId, userId);

        Task task = taskRepository.findByIdWithRelations(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + taskId));

        validateTaskAccess(task, userId);

        task.setDeletedAt(LocalDateTime.now());
        taskRepository.save(task);
        log.info("Task soft deleted successfully: {}", taskId);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"tasks", "taskDetails"}, allEntries = true)
    public List<GetTaskResponse> updateMultipleTasksStatus(List<Long> taskIds, Long userId, Task.TaskStatus status) {
        log.info("Updating multiple tasks status: {} to {} for user: {}", taskIds, status, userId);

        List<Task> tasks = taskRepository.findAllById(taskIds);

        // Validate access for all tasks
        tasks.forEach(task -> validateTaskAccess(task, userId));

        // Update status for all tasks
        tasks.forEach(task -> {
            task.setStatus(status);
            if (status == Task.TaskStatus.completed) {
                task.setCompletedAt(LocalDateTime.now());
                task.setProgress(100);
            } else if (status == Task.TaskStatus.in_progress && task.getProgress() == 0) {
                task.setProgress(10);
            } else if (status != Task.TaskStatus.completed) {
                task.setCompletedAt(null);
            }
        });

        List<Task> updatedTasks = taskRepository.saveAll(tasks);
        log.info("Multiple tasks status updated successfully: {}", taskIds);

        return updatedTasks.stream()
                .map(GetTaskResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @CacheEvict(value = {"tasks", "taskDetails"}, allEntries = true)
    public void deleteMultipleTasks(List<Long> taskIds, Long userId) {
        log.info("Deleting multiple tasks: {} for user: {}", taskIds, userId);

        List<Task> tasks = taskRepository.findAllById(taskIds);

        // Validate access for all tasks
        tasks.forEach(task -> validateTaskAccess(task, userId));

        taskRepository.deleteAll(tasks);
        log.info("Multiple tasks deleted successfully: {}", taskIds);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<GetTaskResponse> searchTasks(Long userId, String keyword, TaskFilterRequest filter) {
        log.info("Searching tasks for user: {} with keyword: {}", userId, keyword);

        Pageable pageable = createPageable(filter);
        Page<Task> tasks = taskRepository.searchTasksByKeyword(userId, keyword, pageable);

        return tasks.map(GetTaskResponse::from);
    }

    // Helper methods
    private void validateTaskAccess(Task task, Long userId) {
        if (!task.getUser().getId().equals(userId) &&
                (task.getAssignedTo() == null || !task.getAssignedTo().getId().equals(userId))) {
            throw new TaskAccessDeniedException("Access denied to task with id: " + task.getId());
        }
    }

    private Pageable createPageable(TaskFilterRequest filter) {
        Sort sort = Sort.by(
                "desc".equalsIgnoreCase(filter.getSortDirection()) ? Sort.Direction.DESC : Sort.Direction.ASC,
                filter.getSortBy());

        return PageRequest.of(filter.getPage(), filter.getSize(), sort);
    }

    private boolean isUserAllowToAccessTask(Task task, Long userId){
        return task.getUser().getId().equals(userId) ||
                (task.getAssignedTo() !=null && task.getAssignedTo().getId().equals(userId));
    }

}