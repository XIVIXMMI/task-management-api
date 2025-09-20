package com.omori.taskmanagement.service.task.query;

import com.omori.taskmanagement.dto.project.task.TaskFilterRequest;
import com.omori.taskmanagement.dto.project.task.TaskResponse;
import com.omori.taskmanagement.exceptions.task.TaskAccessDeniedException;
import com.omori.taskmanagement.exceptions.task.TaskNotFoundException;
import com.omori.taskmanagement.model.project.Task;
import jakarta.annotation.Nullable;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service interface for querying and retrieving task information following the Single Responsibility Principle.
 *
 * <p>This service is responsible exclusively for read operations on tasks, providing various methods
 * to retrieve task data based on different criteria such as user ownership, status, priority, due dates,
 * and search keywords. All operations enforce proper access control to ensure users can only access
 * tasks they own or are assigned to.</p>
 *
 * <h3>Core Responsibilities:</h3>
 * <ul>
 *   <li><strong>Individual Task Retrieval:</strong> Get tasks by ID or UUID with access validation</li>
 *   <li><strong>Filtered Queries:</strong> Retrieve tasks with pagination, sorting, and filtering</li>
 *   <li><strong>Status-based Queries:</strong> Filter tasks by completion status and priority levels</li>
 *   <li><strong>Time-based Queries:</strong> Find overdue, due today, or recently updated tasks</li>
 *   <li><strong>Search Operations:</strong> Full-text search across task titles and descriptions</li>
 * </ul>
 *
 * <h3>Access Control:</h3>
 * <p>All methods enforce access control rules where users can only retrieve:</p>
 * <ul>
 *   <li>Tasks they created (task.user equals requesting user)</li>
 *   <li>Tasks assigned to them (task.assignedTo equals requesting user)</li>
 *   <li>Tasks in shared workspaces where they have appropriate permissions</li>
 * </ul>
 *
 * <h3>Performance Considerations:</h3>
 * <ul>
 *   <li>All paginated queries support configurable page sizes and sorting</li>
 *   <li>Individual task retrievals utilize caching for frequently accessed data</li>
 *   <li>Complex queries leverage database indexes for optimal performance</li>
 * </ul>
 *
 * <h3>Usage Examples:</h3>
 * <pre>{@code
 * // Get a specific task
 * TaskResponse task = taskQueryService.getTaskById(123L, userId);
 *
 * // Get user's overdue tasks
 * Page<TaskResponse> overdueTasks = taskQueryService.getOverdueTasks(userId);
 *
 * // Search tasks with filtering
 * TaskFilterRequest filter = new TaskFilterRequest();
 * filter.setStatus(TaskStatus.IN_PROGRESS);
 * Page<TaskResponse> results = taskQueryService.searchTasks(userId, "urgent", filter);
 * }</pre>
 *
 * @since 1.0.0
 * @see TaskFilterRequest
 * @see TaskResponse
 * @see Task.TaskStatus
 * @see Task.TaskPriority
 */
public interface TaskQueryService {

    /**
     * Retrieves a task by its unique identifier with access control validation.
     *
     * <p>Fetches a single task including all related entities (assignee, category, workspace)
     * and validates that the requesting user has permission to access the task. The task
     * data is returned as a {@link TaskResponse} DTO with complete relationship information.</p>
     *
     * <p><strong>Access Control:</strong> The user must either be the task owner or assigned to the task.
     * If the user lacks access, a {@link TaskAccessDeniedException} is thrown.</p>
     *
     * @param taskId the unique identifier of the task to retrieve
     * @param userId the ID of the user requesting the task data
     * @return a {@link TaskResponse} containing the task data and relationships
     * @throws TaskNotFoundException if no task exists with the given ID
     * @throws TaskAccessDeniedException if the user lacks permission to access the task
     * @since 1.0.0
     */
    TaskResponse getTaskById(Long taskId, Long userId);

    /**
     * Retrieves a task by its UUID with access control validation.
     *
     * <p>Similar to {@link #getTaskById(Long, Long)} but uses the task's UUID instead of
     * the database ID. UUIDs are typically used in API responses and external integrations
     * to avoid exposing internal database identifiers.</p>
     *
     * <p><strong>Performance Note:</strong> UUID lookups may be slightly slower than ID-based
     * lookups depending on database indexing strategy.</p>
     *
     * @param uuid the UUID of the task to retrieve
     * @param userId the ID of the user requesting the task data
     * @return a {@link TaskResponse} containing the task data and relationships
     * @throws TaskNotFoundException if no task exists with the given UUID
     * @throws TaskAccessDeniedException if the user lacks permission to access the task
     * @since 1.0.0
     */
    TaskResponse getTaskByUuid(UUID uuid, Long userId);

    /**
     * Retrieves a paginated list of tasks for a specific user with optional filtering and sorting.
     *
     * <p>Returns all tasks that the user can access (owned or assigned) with support for
     * comprehensive filtering, sorting, and pagination. The filter parameter allows clients
     * to specify criteria such as status, priority, date ranges, and sort preferences.</p>
     *
     * <p><strong>Default Behavior:</strong></p>
     * <ul>
     *   <li>Includes both owned and assigned tasks</li>
     *   <li>Excludes soft-deleted tasks</li>
     *   <li>Sorts by creation date descending if no sort specified</li>
     *   <li>Uses default page size of 20 if not specified</li>
     * </ul>
     *
     * @param userId the ID of the user whose tasks to retrieve
     * @param filter the filtering and pagination criteria, or null for defaults
     * @return a {@link Page} of {@link TaskResponse} objects matching the criteria
     * @since 1.0.0
     * @see TaskFilterRequest
     */
    Page<TaskResponse> getTasksByUserId(Long userId, @Nullable TaskFilterRequest filter);

    /**
     * Retrieves a paginated list of overdue tasks for a specific user.
     *
     * <p>Returns tasks where the due date has passed and the task is not yet completed.
     * Only tasks that the user owns or is assigned to are included. Tasks without
     * due dates are excluded from overdue calculations.</p>
     *
     * <p><strong>Overdue Criteria:</strong></p>
     * <ul>
     *   <li>dueDate is not null and is before current timestamp</li>
     *   <li>status is not COMPLETED or CANCELLED</li>
     *   <li>task is not soft-deleted</li>
     * </ul>
     *
     * @param userId the ID of the user whose overdue tasks to retrieve
     * @return a {@link Page} of overdue {@link TaskResponse} objects
     * @since 1.0.0
     */
    Page<TaskResponse> getOverdueTasks(Long userId, TaskFilterRequest filter);

    /**
     * Performs a full-text search across user's tasks with optional additional filtering.
     *
     * <p>Searches task titles, descriptions, and associated metadata for the specified
     * keyword using database full-text search capabilities. The search is case-insensitive
     * and supports partial word matching. Additional filtering can be applied through
     * the filter parameter.</p>
     *
     * <p><strong>Search Scope:</strong></p>
     * <ul>
     *   <li>Task title (weighted higher in relevance scoring)</li>
     *   <li>Task description</li>
     *   <li>Category names (if associated)</li>
     *   <li>Workspace names (if associated)</li>
     * </ul>
     *
     * @param userId the ID of the user whose tasks to search
     * @param keyword the search term to match against task content
     * @param filter additional filtering criteria, or null for search-only results
     * @return a {@link Page} of {@link TaskResponse} objects matching the search criteria
     * @since 1.0.0
     */
    Page<TaskResponse> searchTasks(Long userId, String keyword, TaskFilterRequest filter);

    /**
     * Retrieves tasks filtered by a specific completion status.
     *
     * <p>Returns all tasks accessible to the user that match the specified status.
     * This method is useful for dashboard views and status-specific workflows.</p>
     *
     * @param userId the ID of the user whose tasks to filter
     * @param status the task status to filter by (PENDING, IN_PROGRESS, COMPLETED, etc.)
     * @return a {@link Page} of {@link TaskResponse} objects with the specified status
     * @since 1.0.0
     * @see Task.TaskStatus
     */
    Page<TaskResponse> getTasksByStatus(Long userId, Task.TaskStatus status, TaskFilterRequest filter);

    /**
     * Retrieves tasks filtered by a specific priority level.
     *
     * <p>Returns all tasks accessible to the user that match the specified priority.
     * Useful for priority-based task management and urgent task identification.</p>
     *
     * @param userId the ID of the user whose tasks to filter
     * @param priority the priority level to filter by (LOW, MEDIUM, HIGH, CRITICAL)
     * @return a {@link Page} of {@link TaskResponse} objects with the specified priority
     * @since 1.0.0
     * @see Task.TaskPriority
     */
    Page<TaskResponse> getTasksByPriority(Long userId, Task.TaskPriority priority, TaskFilterRequest filter);

    /**
     * Retrieves tasks that are due today for a specific user.
     *
     * <p>Returns tasks where the due date falls within the current calendar day
     * (midnight to 23:59:59) in the system's default timezone. Only incomplete
     * tasks are included in the results.</p>
     *
     * <p><strong>Due Today Criteria:</strong></p>
     * <ul>
     *   <li>dueDate is within today's date range</li>
     *   <li>status is not COMPLETED or CANCELLED</li>
     *   <li>task is not soft-deleted</li>
     * </ul>
     *
     * @param userId the ID of the user whose due-today tasks to retrieve
     * @return a {@link Page} of {@link TaskResponse} objects due today
     * @since 1.0.0
     */
    Page<TaskResponse> getTasksDueToday(Long userId, TaskFilterRequest filter);

    /**
     * Retrieves tasks with due dates within a specific date range.
     *
     * <p>Returns tasks where the due date falls between the specified start and end
     * timestamps (inclusive). This method is useful for calendar views and
     * date-range based task planning.</p>
     *
     * <p><strong>Date Range Behavior:</strong></p>
     * <ul>
     *   <li>start and end timestamps are inclusive</li>
     *   <li>Tasks with null due dates are excluded</li>
     *   <li>Results include all task statuses unless filtered otherwise</li>
     * </ul>
     *
     * @param userId the ID of the user whose tasks to retrieve
     * @param start the start of the date range (inclusive)
     * @param end the end of the date range (inclusive)
     * @return a {@link Page} of {@link TaskResponse} objects due within the specified range
     * @throws IllegalArgumentException if start is after end
     * @since 1.0.0
     */
    Page<TaskResponse> getTasksDueBetween(Long userId, LocalDateTime start, LocalDateTime end, TaskFilterRequest filter);

    /**
     * Retrieves tasks that have been updated within a specified number of days.
     *
     * <p>Returns tasks that have been modified (any field change) within the last
     * N days from the current timestamp. This includes creation, updates, status changes,
     * and progress modifications. Useful for activity tracking and recent changes review.</p>
     *
     * <p><strong>Update Tracking:</strong></p>
     * <ul>
     *   <li>Based on task's updatedAt timestamp</li>
     *   <li>Includes all types of modifications</li>
     *   <li>Ordered by most recently updated first</li>
     * </ul>
     *
     * @param userId the ID of the user whose tasks to retrieve
     * @param daysBack the number of days to look back for updates (must be positive)
     * @return a {@link Page} of {@link TaskResponse} objects updated within the timeframe
     * @throws IllegalArgumentException if daysBack is not positive
     * @since 1.0.0
     */
    Page<TaskResponse> getRecentlyUpdatedTasks(Long userId, Integer daysBack, TaskFilterRequest filter);
}
