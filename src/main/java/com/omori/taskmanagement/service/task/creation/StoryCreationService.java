package com.omori.taskmanagement.service.task.creation;

import com.omori.taskmanagement.dto.project.task.creation.BaseTaskCreateRequest;
import com.omori.taskmanagement.model.project.Task;

/**
 * Service interface for creating story-type tasks within the task management system.
 * <p>
 * This service provides specialized methods for creating Story tasks, which sit at the middle level
 * of the task hierarchy (Epic → Story → Task). Stories represent features or user stories that
 * can either exist independently or be grouped under Epics for better organization.
 * </p>
 *
 * <h3>Story Creation Types:</h3>
 * <ul>
 *     <li><b>Standalone Stories:</b> Independent story tasks not associated with any parent Epic</li>
 *     <li><b>Epic Child Stories:</b> Stories created under an existing Epic with hierarchical relationships</li>
 * </ul>
 *
 * <h3>Task Hierarchy Context:</h3>
 * <pre>
 * Epic (Level 1)
 *   └── Story (Level 2) ← This service creates these
 *         └── Task (Level 3)
 * </pre>
 *
 * <h3>Security & Validation:</h3>
 * <p>
 * All story creation methods perform:
 * </p>
 * <ul>
 *     <li>User authentication and authorization checks</li>
 *     <li>Input validation using the BaseTaskCreateRequest DTO</li>
 *     <li>Business rule validation (e.g., workspace access, Epic existence for hierarchical stories)</li>
 *     <li>Task type compatibility validation (ensuring parent Epics can contain Stories)</li>
 *     <li>Automatic field population (created date, creator, task type assignment)</li>
 * </ul>
 *
 * @see BaseTaskCreateRequest
 * @see Task
 * @see Task.TaskType#STORY
 * @see EpicCreationService
 * @see TaskCreationService
 */
public interface StoryCreationService {

    /**
     * Creates a new standalone story task that is not associated with any parent Epic.
     * <p>
     * This method creates an independent story that exists at the second level of the task
     * hierarchy. The story can later have child tasks created under it. Any parentId provided
     * in the request will be rejected (validation error) to ensure the story remains standalone.
     * </p>
     *
     * <h4>Use Cases:</h4>
     * <ul>
     *     <li>Creating user stories that don't belong to a specific Epic</li>
     *     <li>Creating feature stories for small projects that don't require Epic organization</li>
     *     <li>Creating temporary or experimental stories for rapid prototyping</li>
     * </ul>
     *
     * @param userId the ID of the user creating the story, used for authorization and audit logging
     * @param request the story creation request containing all necessary story data and metadata.
     *                The parentId field will be ignored even if provided
     * @return the newly created Story task with task type set to STORY and no parent relationships
     * @throws com.omori.taskmanagement.exceptions.task.TaskAccessDeniedException if the user lacks permission to create stories in the target workspace
     * @throws com.omori.taskmanagement.exceptions.task.TaskValidationException if the request data is invalid or required fields are missing
     * @throws com.omori.taskmanagement.exceptions.workspace.WorkspaceNotFoundException if the workspace specified in the request does not exist
     * @throws com.omori.taskmanagement.exceptions.UserNotFoundException if the user ID does not exist
     */
    Task createStoryTask(Long userId, BaseTaskCreateRequest request);

    /**
     * Creates a new story task under an existing Epic with hierarchical relationships.
     * <p>
     * This method creates a child story that belongs to a parent Epic, establishing
     * a hierarchical relationship. The Epic must exist and be accessible to the user.
     * The story will be positioned within the Epic's child collection with appropriate
     * sort ordering.
     * </p>
     *
     * <h4>Use Cases:</h4>
     * <ul>
     *     <li>Adding user stories to an existing Epic during sprint planning</li>
     *     <li>Breaking down large Epics into manageable Story components</li>
     *     <li>Organizing related features under a common Epic theme</li>
     * </ul>
     *
     * <h4>Validation Rules:</h4>
     * <ul>
     *     <li>The parentId in the request must not be null</li>
     *     <li>The parent task must exist and be of type EPIC</li>
     *     <li>The user must have access to both the workspace and the parent Epic</li>
     *     <li>The Epic must be able to contain Story-type children (business rule validation)</li>
     * </ul>
     *
     * @param userId the ID of the user creating the story, used for authorization and audit logging
     * @param type the specific task type to create (should be STORY, but allows flexibility for future subtypes)
     * @param request the story creation request containing all necessary story data and metadata.
     *                Must include a valid parentId pointing to an existing Epic
     * @return the newly created Story task with parent-child relationships established and sort order assigned
     * @throws com.omori.taskmanagement.exceptions.task.TaskValidationException if parentId is null or request data is invalid
     * @throws com.omori.taskmanagement.exceptions.task.TaskNotFoundException if the parent Epic specified in parentId does not exist
     * @throws com.omori.taskmanagement.exceptions.task.InvalidTaskTypeException if the parent task is not an Epic or type compatibility fails
     * @throws com.omori.taskmanagement.exceptions.task.TaskAccessDeniedException if the user lacks permission to create stories under the target Epic
     * @throws com.omori.taskmanagement.exceptions.UserNotFoundException if the user ID does not exist
     */
    Task createStoryUnderEpic(Long userId, Task.TaskType type, BaseTaskCreateRequest request);
}
