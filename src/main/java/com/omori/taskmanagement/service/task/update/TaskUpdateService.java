package com.omori.taskmanagement.service.task.update;

import com.omori.taskmanagement.dto.project.task.TaskResponse;
import com.omori.taskmanagement.dto.project.task.update.TaskUpdateRequest;
import com.omori.taskmanagement.exceptions.task.TaskAccessDeniedException;
import com.omori.taskmanagement.exceptions.task.TaskNotFoundException;
import com.omori.taskmanagement.exceptions.task.TaskBusinessException;
import com.omori.taskmanagement.exceptions.UserNotFoundException;
import com.omori.taskmanagement.exceptions.task.WorkspaceNotFoundException;

/**
 * Service interface for updating task information following the Single Responsibility Principle.
 *
 * <p>This service is responsible exclusively for task modification operations, providing methods
 * to update task data including basic fields, relationships, and business logic. All operations
 * enforce proper access control to ensure users can only modify tasks they own or are assigned to.</p>
 *
 * <h3>Core Responsibilities:</h3>
 * <ul>
 *   <li><strong>Task Field Updates:</strong> Modify basic task properties with partial update strategy</li>
 *   <li><strong>Relationship Management:</strong> Update task associations (category, assignee, workspace)</li>
 *   <li><strong>Business Logic:</strong> Handle status transitions and completion date management</li>
 *   <li><strong>Cache Management:</strong> Maintain cache consistency after modifications</li>
 * </ul>
 *
 * <h3>Access Control:</h3>
 * <p>All methods enforce access control rules where users can only modify:</p>
 * <ul>
 *   <li>Tasks they created (task.user equals requesting user)</li>
 *   <li>Tasks assigned to them (task.assignedTo equals requesting user)</li>
 *   <li>Tasks in shared workspaces where they have appropriate permissions</li>
 * </ul>
 *
 * <h3>Update Strategy:</h3>
 * <ul>
 *   <li>Partial updates - only non-null fields in requests are applied</li>
 *   <li>Automatic business rule enforcement (status/progress consistency)</li>
 *   <li>Transactional updates with proper rollback handling</li>
 * </ul>
 *
 * <h3>Usage Examples:</h3>
 * <pre>{@code
 * // Update basic task fields
 * TaskUpdateRequest request = TaskUpdateRequest.builder()
 *     .title("Updated Title")
 *     .status(TaskStatus.IN_PROGRESS)
 *     .progress(50)
 *     .build();
 * TaskResponse updated = taskUpdateService.updateTask(taskId, userId, request);
 *
 * // Update only relationships
 * TaskUpdateRequest relationshipUpdate = TaskUpdateRequest.builder()
 *     .categoryId(newCategoryId)
 *     .assignedToId(newAssigneeId)
 *     .build();
 * TaskResponse updated = taskUpdateService.updateTask(taskId, userId, relationshipUpdate);
 * }</pre>
 *
 * @since 1.0.0
 * @see TaskUpdateRequest
 * @see TaskResponse
 * @see Task.TaskStatus
 * @see Task.TaskPriority
 */
public interface TaskUpdateService {

    /**
     * Updates a task with the provided data using partial update strategy.
     *
     * <p>Performs a comprehensive task update operation that includes loading and validating
     * the task with access control, updating basic fields and relationships, applying business
     * logic, and maintaining cache consistency. Only non-null fields in the request are applied
     * to the task entity, allowing for granular updates without affecting unchanged fields.</p>
     *
     * <p><strong>Business Rules Applied:</strong> Status transitions automatically adjust progress
     * values and completion dates. Progress consistency is maintained when status changes.</p>
     *
     * <p><strong>Access Control:</strong> The user must either be the task owner or assigned to the task.
     * If the user lacks access, a {@link TaskAccessDeniedException} is thrown.</p>
     *
     * @param taskId the unique identifier of the task to update
     * @param userId the ID of the user requesting the update (for access control)
     * @param request the update request containing fields to modify (null fields ignored)
     * @return a {@link TaskResponse} containing the updated task data and relationships
     * @throws TaskNotFoundException if no task exists with the given ID
     * @throws TaskAccessDeniedException if the user lacks permission to modify the task
     * @throws TaskBusinessException if category or workspace references are invalid
     * @throws UserNotFoundException if the assigned user reference is invalid
     * @throws WorkspaceNotFoundException if the workspace reference is invalid
     * @throws IllegalArgumentException if request validation fails (invalid progress, dates, etc.)
     * @since 1.0.0
     */
    TaskResponse updateTask(Long taskId, Long userId, TaskUpdateRequest request);

    /**
     * Updates the cached task data with the provided response.
     *
     * <p>This method maintains cache consistency after task modifications using a cache-aside
     * pattern where the application explicitly manages cache updates after data modifications.
     * The cache key is constructed using the task ID and user ID to ensure proper cache
     * isolation between users.</p>
     *
     * <p><strong>Cache Strategy:</strong> Uses {@code @CachePut} to ensure the cache is updated
     * with the latest task data after modification operations.</p>
     *
     * @param taskId the unique identifier of the task being cached
     * @param userId the ID of the user associated with this cache entry
     * @param response the task response data to store in cache
     * @return the same {@link TaskResponse} that was passed in (for method chaining)
     * @since 1.0.0
     */
    TaskResponse cacheUpdatedTask(Long taskId, Long userId, TaskResponse response);
}
