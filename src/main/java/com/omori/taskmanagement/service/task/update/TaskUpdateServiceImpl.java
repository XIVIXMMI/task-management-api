package com.omori.taskmanagement.service.task.update;

import com.omori.taskmanagement.dto.project.task.TaskResponse;
import com.omori.taskmanagement.dto.project.task.TaskUpdateRequest;
import com.omori.taskmanagement.exceptions.UserNotFoundException;
import com.omori.taskmanagement.exceptions.task.TaskBusinessException;
import com.omori.taskmanagement.exceptions.task.TaskNotFoundException;
import com.omori.taskmanagement.exceptions.task.WorkspaceNotFoundException;
import com.omori.taskmanagement.model.project.Category;
import com.omori.taskmanagement.model.project.Task;
import com.omori.taskmanagement.model.project.Workspace;
import com.omori.taskmanagement.model.usermgmt.User;
import com.omori.taskmanagement.repository.project.CategoryRepository;
import com.omori.taskmanagement.repository.project.TaskRepository;
import com.omori.taskmanagement.repository.project.WorkspaceRepository;
import com.omori.taskmanagement.repository.usermgmt.UserRepository;
import com.omori.taskmanagement.service.task.utils.TaskAccessControlService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Slf4j
@lombok.RequiredArgsConstructor
public class TaskUpdateServiceImpl implements TaskUpdateService{

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final CategoryRepository categoryRepository;
    private final WorkspaceRepository workspaceRepository;

    private final TaskAccessControlService taskAccessControlService;

    @Override
    @Transactional
    public TaskResponse updateTask(Long taskId, Long userId, TaskUpdateRequest request) {
        log.info("Updating task with id: {} for user {}", taskId, userId);

        Task task = loadAndValidateTask(taskId, userId);

        validateUpdateRequest(request);

        updateBasicFields(task, request);
        updateRelatedEntities(task, request);
        updateBusinessLogic(task, request);

        Task updatedTask = taskRepository.save(task);
        log.info("Task updated successfully: {}", taskId);

        TaskResponse response = TaskResponse.from(updatedTask);
        return cacheUpdatedTask(taskId, userId, response);
    }

    @Override
    @CachePut(value = "task-details", key = "#taskId + ':' + #userId")
    public TaskResponse cacheUpdatedTask(Long taskId, Long userId, TaskResponse response) {
        return response;
    }

    private Task loadAndValidateTask(Long taskId, Long userId) {
        Task task = taskRepository.findByIdWithRelations(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + taskId));

        taskAccessControlService.validateTaskAccess(task, userId);
        return task;
    }

    private void updateBasicFields(Task task, TaskUpdateRequest request) {
        if (request.getTitle() != null) task.setTitle(request.getTitle());
        if (request.getDescription() != null) task.setDescription(request.getDescription());
        if (request.getDueDate() != null) task.setDueDate(request.getDueDate());
        if (request.getStartDate() != null ) task.setStartDate(request.getStartDate());
        if (request.getPriority() != null ) task.setPriority(request.getPriority());
        if (request.getStatus() != null ) task.setStatus(request.getStatus());
        if (request.getEstimatedHours() != null ) task.setEstimatedHours(request.getEstimatedHours());
        if (request.getActualHours() != null ) task.setActualHours(request.getActualHours());
        if (request.getProgress() != null ) task.setProgress(request.getProgress());
        if (request.getSortOrder() != null ) task.setSortOrder(request.getSortOrder());
        if (request.getIsRecurring() != null ) task.setIsRecurring(request.getIsRecurring());
        if (request.getRecurrencePattern() != null ) task.setRecurrencePattern(request.getRecurrencePattern());
        if (request.getMetadata() != null ) task.setMetadata(request.getMetadata());
    }

    private void updateRelatedEntities(Task task, TaskUpdateRequest request) {
        updateTaskCategory(task, request.getCategoryId());
        updateAssignedUser(task, request.getAssignedToId());
        updateWorkspace(task, request.getWorkspaceId());
    }

    private void updateTaskCategory(Task task, Long categoryId) {
        if (categoryId != null) {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new TaskBusinessException("Category not found with id: " + categoryId));
            task.setCategory(category);
        }
    }

    private void updateAssignedUser(Task task, Long assignedToId) {
        if (assignedToId != null) {
            User assignedTo = userRepository.findById(assignedToId)
                    .orElseThrow(() -> new UserNotFoundException("Assigned user not found with id: " + assignedToId));
            task.setAssignedTo(assignedTo);
        }
    }

    private void updateWorkspace(Task task, Long workspaceId) {
        if (workspaceId != null) {
            Workspace workspace = workspaceRepository.findById(workspaceId)
                    .orElseThrow(() -> new WorkspaceNotFoundException("Workspace not found with id: " + workspaceId));
            task.setWorkspace(workspace);
        }
    }

    private void updateBusinessLogic(Task task, TaskUpdateRequest request) {
        updateCompletionDate(task, request.getStatus());
        updateProgressConsistency(task, request.getStatus(), request.getProgress());
        triggerWorkflowEvents(task, request); // For future workflow integration
        recordTaskUpdate(task, request); // For audit purposes
    }

    private void recordTaskUpdate(Task task, TaskUpdateRequest request) {
    }

    private void triggerWorkflowEvents(Task task, TaskUpdateRequest request) {
    }

    private void updateProgressConsistency(Task task, Task.@NotNull(message = "Status is required") TaskStatus status, @Min(value = 0, message = "Progress must be between 0 and 100") @Max(value = 100, message = "Progress must be between 0 and 100") Integer progress) {
        if (status == Task.TaskStatus.completed && (progress == null || progress < 100)) {
            task.setProgress(100);
        } else if (status == Task.TaskStatus.pending && progress != null && progress > 0) {
            task.setStatus(Task.TaskStatus.in_progress);
        }
    }

    private void updateCompletionDate(Task task, Task.TaskStatus newStatus) {
        if (newStatus == Task.TaskStatus.completed && task.getCompletedAt() == null) {
            task.setCompletedAt(LocalDateTime.now());
        } else if (newStatus != Task.TaskStatus.completed) {
            task.setCompletedAt(null);
        }
    }

    private void validateUpdateRequest(TaskUpdateRequest request) {
        if (request.getProgress() != null && (request.getProgress() < 0 || request.getProgress()
                > 100)) {
            throw new IllegalArgumentException("Progress must be between 0 and 100");
        }
        if (request.getStartDate() != null && request.getDueDate() != null
                && request.getStartDate().isAfter(request.getDueDate())) {
            throw new IllegalArgumentException("Start date cannot be after due date");
        }
    }
}
