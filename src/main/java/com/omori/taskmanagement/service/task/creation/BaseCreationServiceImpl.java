package com.omori.taskmanagement.service.task.creation;

import com.omori.taskmanagement.dto.project.task.creation.BaseTaskCreateRequest;
import com.omori.taskmanagement.exceptions.UserNotFoundException;
import com.omori.taskmanagement.exceptions.task.InvalidTaskTypeException;
import com.omori.taskmanagement.exceptions.task.TaskBusinessException;
import com.omori.taskmanagement.exceptions.task.TaskNotFoundException;
import com.omori.taskmanagement.model.project.Task;
import com.omori.taskmanagement.model.usermgmt.User;
import com.omori.taskmanagement.repository.project.TaskRepository;
import com.omori.taskmanagement.repository.usermgmt.UserRepository;
import com.omori.taskmanagement.service.task.hierarchy.TaskHierarchyService;
import com.omori.taskmanagement.service.task.utils.TaskRelationsService;
import com.omori.taskmanagement.service.task.utils.TaskValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BaseCreationServiceImpl implements BaseCreationService{

    private final TaskValidationService taskValidationService;
    private final TaskRelationsService taskRelationsService;
    private final TaskHierarchyService taskHierarchyService;

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;

    private static final Double DEFAULT_ACTUAL_HOURS = 0.0;
    private static final Integer DEFAULT_PROGRESS = 0;
    private static final Integer DEFAULT_SORT_ORDER = 0;

    private static final LocalDateTime DEFAULT_START_DATE = LocalDateTime.now();
    private static final LocalDateTime DEFAULT_DUE_DATE = DEFAULT_START_DATE.plusDays(7);

    private static final Double DEFAULT_ESTIMATED_HOURS = 1.0;


    @Override
    @Transactional
    public Task createTask(Long userId, Task.TaskType type, BaseTaskCreateRequest request, boolean ignoreParent) {
        if (type == null ) {
            throw new InvalidTaskTypeException("Task type must be provided in request body");
        }
        log.debug("Creating hierarchical task for user {} ", userId);

        taskValidationService.validateCreateTaskRequest(request, userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User " + userId + " not found"));

        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .dueDate(Optional.ofNullable(request.getDueDate()).orElse(DEFAULT_DUE_DATE))
                .startDate(Optional.ofNullable(request.getStartDate()).orElse(DEFAULT_START_DATE))
                .priority(Optional.ofNullable(request.getPriority()).orElse(Task.TaskPriority.medium))
                .status(Task.TaskStatus.pending)
                .taskType(type)
                .estimatedHours(Optional.ofNullable(request.getEstimatedHours()).orElse(DEFAULT_ESTIMATED_HOURS))
                .actualHours(DEFAULT_ACTUAL_HOURS)
                .progress(DEFAULT_PROGRESS)
                .user(user)
                .sortOrder(Optional.ofNullable(request.getSortOrder()).orElse(DEFAULT_SORT_ORDER))
                .isRecurring(Optional.ofNullable(request.getIsRecurring()).orElse(false))
//                .recurrencePattern(request.getRecurrencePattern())
//                .metadata(request.getMetadata())
                .build();

        taskRelationsService.setTaskRelations(task,
                request.getCategoryId(),
                request.getAssignedToId(),
                request.getWorkspaceId());

        // Set a parent task if creating STORY or EPIC
        if(!ignoreParent && request.getParentId() != null){
            Task parentTask = taskRepository.findById(request.getParentId())
                    .orElseThrow(() -> new TaskNotFoundException("Parent task not found with ID: "
                            + request.getParentId()));

            // Validate hierarchy rules
            if(!parentTask.getTaskType().canContain(type)){
                throw new InvalidTaskTypeException(
                        String.format("%s cannot contain %s", parentTask.getTaskType(), type)
                );
            }
            task.setParentTask(parentTask);
            task.setSortOrder(taskHierarchyService.getNextSortOrderForParent(request.getParentId()));
        }

        try {
            Task savedTask = taskRepository.save(task);
            log.debug("Task created successfully with ID {} for user {}",
                    savedTask.getId(), userId);
            return savedTask;
        } catch (DataAccessException e) {
            log.error("Failed to save task for user {}: {}", userId, e.getMessage());
            throw new TaskBusinessException("Failed to create task", e);
        }
    }
}
