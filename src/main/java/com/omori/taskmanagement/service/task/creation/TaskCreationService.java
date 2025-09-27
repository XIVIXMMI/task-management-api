package com.omori.taskmanagement.service.task.creation;

import com.omori.taskmanagement.dto.project.task.creation.BaseTaskCreateRequest;
import com.omori.taskmanagement.model.project.Task;

/**
 * Service interface for creating tasks within the task management system.
 * <p>
 * This service provides methods for creating different types of tasks including standalone tasks
 * and tasks that are part of a story hierarchy. It handles task creation with proper validation,
 * access control, and relationship management.
 * </p>
 *
 * <h3>Task Creation Types:</h3>
 * <ul>
 *     <li><b>Standalone Tasks:</b> Independent tasks not associated with any parent story</li>
 *     <li><b>Story Child Tasks:</b> Tasks created under an existing story with hierarchical relationships</li>
 * </ul>
 *
 * <h3>Security & Validation:</h3>
 * <p>
 * All task creation methods perform:
 * </p>
 * <ul>
 *     <li>User authentication and authorization checks</li>
 *     <li>Input validation using the TaskCreateRequest DTO</li>
 *     <li>Business rule validation (e.g., workspace access, task type compatibility)</li>
 *     <li>Automatic field population (created date, creator, UUID generation)</li>
 * </ul>
 *
 * @see BaseTaskCreateRequest
 * @see Task
 * @see Task.TaskType
 */
public interface TaskCreationService {

    /**
     * Creates a new task under an existing story with the specified task type.
     * <p>
     * This method creates a child task that belongs to a parent story, establishing
     * a hierarchical relationship. The task type must be compatible with story-child
     * relationships (typically TASK or SUBTASK types).
     * </p>
     *
     * @param userId the ID of the user creating the task, used for authorization and audit logging
     * @param type the specific task type to create (must be compatible with story hierarchy)
     * @param request the task creation request containing all necessary task data and metadata
     * @return the newly created Task entity with all relationships established
     * @throws TaskAccessDeniedException if the user lacks permission to create tasks in the target workspace
     * @throws TaskValidationException if the request data is invalid or task type is incompatible
     * @throws StoryNotFoundException if the parent story specified in the request does not exist
     * @throws TaskTypeIncompatibilityException if the task type cannot be created under a story
     */
    Task createTaskUnderStory(Long userId, Task.TaskType type, BaseTaskCreateRequest request);

    /**
     * Creates a new standalone task that is not associated with any parent story.
     * <p>
     * This method creates an independent task that exists at the top level of the task
     * hierarchy. The task type is automatically determined from the request or defaults
     * to the standard TASK type.
     * </p>
     *
     * @param userId the ID of the user creating the task, used for authorization and audit logging
     * @param request the task creation request containing all necessary task data and metadata
     * @return the newly created Task entity with generated UUID and audit fields populated
     * @throws TaskAccessDeniedException if the user lacks permission to create tasks in the target workspace
     * @throws TaskValidationException if the request data is invalid or required fields are missing
     * @throws WorkspaceNotFoundException if the workspace specified in the request does not exist
     */
    Task createStandaloneTask(Long userId, BaseTaskCreateRequest request);
}
