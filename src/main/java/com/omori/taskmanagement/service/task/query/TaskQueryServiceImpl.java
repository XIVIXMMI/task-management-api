package com.omori.taskmanagement.service.task.query;

import com.omori.taskmanagement.dto.project.task.TaskFilterRequest;
import com.omori.taskmanagement.dto.project.task.TaskResponse;
import com.omori.taskmanagement.exceptions.task.TaskNotFoundException;
import com.omori.taskmanagement.model.project.Task;
import com.omori.taskmanagement.repository.project.TaskRepository;
import com.omori.taskmanagement.service.task.TaskAccessControlService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskQueryServiceImpl implements TaskQueryService{

    private final TaskRepository taskRepository;
    private final TaskAccessControlService taskAccessControlService;
    private final TaskFilterConfigService taskFilterConfigService;

    // Follow DRY principle
    private TaskResponse getSingleTask(
            Supplier<Optional<Task>> taskSupplier,
            String errorMessage,
            Long userId) {
        log.info("Getting task for user {}", userId);
        Task task = taskSupplier.get()
                .orElseThrow(() -> new TaskNotFoundException(errorMessage));
        taskAccessControlService.validateTaskAccess(task, userId);
        return TaskResponse.from(task);
    }

    private Page<TaskResponse> getPaginatedTasks (
            String operator,
            Long userid,
            TaskFilterRequest filterRequest,
            TaskQueryFunction queryFunction
    ){
        log.debug("Executing {} for user: {}",operator,userid);
        TaskFilterRequest effectiveFilter = taskFilterConfigService.resolveFilter(filterRequest);
        Pageable pageable = taskFilterConfigService.createPageable(effectiveFilter);
        Page<Task> tasks = queryFunction.execute(userid,pageable);
        return tasks.map(TaskResponse::from);
    }

    // Functional interface to pass query logic
    @FunctionalInterface
    private interface TaskQueryFunction {
        Page<Task> execute(Long userId,Pageable pageable);
    }

    @Override
    @Cacheable(
            value = "task-details",
            key = "#taskId + ':' + #userId",
            condition = "#taskId != null",
            unless = "#result == null"
    )
    public TaskResponse getTaskById(Long taskId, Long userId) {
        return getSingleTask(
                () -> taskRepository.findByIdWithRelations(taskId),
                String.format("Task not found with id: %d", taskId),
                userId
        );
    }

    @Override
    @Cacheable(
            value = "task-details",
            key = "#uuid + ':' + #userId",
            condition = "#uuid != null",
            unless = "#result == null"
    )
    public TaskResponse getTaskByUuid(UUID uuid, Long userId) {
        return getSingleTask(
                () -> taskRepository.findByUuidWithRelations(uuid),
                String.format("Task not found with uuid: %s", uuid),
                userId
        );
    }

    @Override
    public Page<TaskResponse> getTasksByUserId(Long userId, TaskFilterRequest filter) {
        return getPaginatedTasks(
                "Getting All Tasks",
                userId,
                filter,
                taskRepository::findByUserIdAndNotDeleted // mapping to functional interface
        );
    }

    @Override
    public Page<TaskResponse> getOverdueTasks(Long userId, TaskFilterRequest filter) {
        return getPaginatedTasks(
                "Getting overdue tasks for user: " + userId,
                userId,
                filter,
                (uid, pageable) -> taskRepository.findOverdueTasksByUserId(
                        uid,
                        LocalDateTime.now(),
                        pageable
                )
        );
    }

    @Override
    public Page<TaskResponse> searchTasks(Long userId, String keyword, TaskFilterRequest filter) {
        return getPaginatedTasks(
                "Searching tasks for user " + userId + " with keyword: " + keyword,
                userId,
                filter,
                (uid, pageable) -> taskRepository.searchTasksByKeyword(
                        uid,
                        keyword,
                        pageable
                )
        );
    }

    @Override
    public Page<TaskResponse> getTasksByStatus(Long userId, Task.TaskStatus status, TaskFilterRequest filter) {
        return getPaginatedTasks(
                "Getting tasks by status for user: " + userId + " with status " + status,
                userId,
                filter,
                (uid, pageable) -> taskRepository.findActiveTasksByStatus(
                        uid,
                        status,
                        pageable
                )
        );
    }

    @Override
    public Page<TaskResponse> getTasksByPriority(Long userId, Task.TaskPriority priority, TaskFilterRequest filter) {
        return getPaginatedTasks(
                "Getting task for user " + userId + " with priority " + priority,
                userId,
                filter,
                (uid,pageable) -> taskRepository.findActiveTasksByPriority(
                        uid,
                        priority,
                        pageable
                )
        );
    }

    @Override
    public Page<TaskResponse> getTasksDueToday(Long userId, TaskFilterRequest filter) {
        LocalDate today = LocalDate.now();
        LocalDateTime endOfDay = today.atTime(23, 59, 59);
        return getPaginatedTasks(
                "Getting all task due today for user " + userId,
                userId,
                filter,
                (uid, pageable) -> taskRepository.findActiveTasksByDueDay(
                        uid,
                        endOfDay,
                        pageable
                )
        );
    }

    @Override
    public Page<TaskResponse> getTasksDueBetween(Long userId, LocalDateTime start, LocalDateTime end, TaskFilterRequest filter) {
        return getPaginatedTasks(
                "Getting all tasks due between " + start + " and " + end + " for user " + userId,
                userId,
                filter,
                (uid,pageable) -> taskRepository.findActiveTaskBetweenDueDay(
                        uid,
                        start,
                        end,
                        pageable
                )
        );
    }

    @Override
    public Page<TaskResponse> getRecentlyUpdatedTasks(Long userId, int daysBack, TaskFilterRequest filter) {
        return getPaginatedTasks(
                "Getting all recently updated task for user " + userId + " with days back: " + daysBack,
                userId,
                filter,
                (uid,pageable)-> taskRepository.findActiveTasksFromNowToDaysBack(
                        uid,
                        daysBack,
                        pageable
                )
        );
    }
}
