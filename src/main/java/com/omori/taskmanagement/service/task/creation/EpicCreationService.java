package com.omori.taskmanagement.service.task.creation;

import com.omori.taskmanagement.dto.project.task.creation.BaseTaskCreateRequest;
import com.omori.taskmanagement.dto.project.task.creation.EpicCreateRequest;
import com.omori.taskmanagement.dto.project.task.creation.EpicWithStoriesRequest;
import com.omori.taskmanagement.exceptions.UserNotFoundException;
import com.omori.taskmanagement.exceptions.task.InvalidTaskTypeException;
import com.omori.taskmanagement.exceptions.task.TaskBusinessException;
import com.omori.taskmanagement.exceptions.task.TaskValidationException;
import com.omori.taskmanagement.model.project.Task;

public interface EpicCreationService {

    /**
     * Creates a new standalone Epic task at the top level of the task hierarchy.
     * <p>
     * This method creates an independent Epic that serves as the highest level container
     * in the task management hierarchy. Epics are used to group related Stories and
     * represent large initiatives, features, or project phases that span multiple sprints.
     * </p>
     *
     * <h3>Epic Characteristics:</h3>
     * <ul>
     *     <li><b>Task Type:</b> Automatically set to {@code TaskType.EPIC}</li>
     *     <li><b>Hierarchy Position:</b> Top level (no parent task)</li>
     *     <li><b>Children:</b> Can contain Story tasks</li>
     *     <li><b>Purpose:</b> High-level feature or initiative organization</li>
     * </ul>
     *
     * <h3>Task Hierarchy Context:</h3>
     * <pre>
     * Epic (Level 0) ← This method creates these
     *   └── Story (Level 1)
     *         └── Task (Level 2)
     * </pre>
     *
     * <h3>Epic Creation Process:</h3>
     * <ol>
     *     <li>Validates the Epic creation request and user permissions</li>
     *     <li>Creates the Epic with {@code TaskType.EPIC} automatically assigned</li>
     *     <li>Sets Epic as standalone (no parent) by ignoring any parentId in request</li>
     *     <li>Assigns default values for progress, sort order, and timestamps</li>
     *     <li>Establishes workspace and category relationships</li>
     * </ol>
     *
     * <h3>Use Cases:</h3>
     * <ul>
     *     <li>Creating major project initiatives (e.g., "User Authentication System")</li>
     *     <li>Organizing features for product releases</li>
     *     <li>Planning large-scale development efforts</li>
     *     <li>Grouping related user stories for sprint planning</li>
     * </ul>
     *
     * <h3>Field Behavior:</h3>
     * <ul>
     *     <li><b>parentId:</b> Ignored even if provided (Epics are always top-level)</li>
     *     <li><b>taskType:</b> Automatically set to EPIC regardless of request value</li>
     *     <li><b>progress:</b> Defaults to 0, will be calculated from child Stories</li>
     *     <li><b>sortOrder:</b> Assigned automatically for Epic ordering</li>
     * </ul>
     *
     * <h3>Example Usage:</h3>
     * <pre>{@code
     * EpicCreateRequest request = EpicCreateRequest.builder()
     *     .title("E-commerce Platform")
     *     .description("Complete online shopping platform with user auth, catalog, and payments")
     *     .priority(TaskPriority.high)
     *     .dueDate(LocalDateTime.of(2024, 12, 31, 23, 59))
     *     .workspaceId(1L)
     *     .assignedToId(userId)
     *     .build();
     *
     * Task epic = epicCreationService.createEpicTask(userId, request);
     * // epic.getTaskType() == TaskType.EPIC
     * // epic.getParentTask() == null
     * }</pre>
     *
     * @param userId the ID of the user creating the Epic, used for authorization and audit logging
     * @param request the Epic creation request containing all necessary Epic data and metadata.
     *                The taskType and parentId fields will be overridden
     * @return the newly created Epic task with task type set to EPIC and no parent relationships
     * @throws UserNotFoundException if the specified user does not exist
     * @throws TaskValidationException if the request data is invalid or required fields are missing
     * @throws com.omori.taskmanagement.exceptions.workspace.WorkspaceNotFoundException if the workspace specified in the request does not exist
     * @throws TaskBusinessException if database operation fails during Epic creation
     * @since 1.0.0
     * @see EpicWithStoriesRequest
     * @see Task.TaskType#EPIC
     * @see #createEpicWithInitialStories(Long, EpicWithStoriesRequest)
     */
    Task createEpicTask(Long userId, EpicCreateRequest request);

    /**
     * Creates an Epic task with initial story tasks in a single transaction.
     *
     * <p>This method creates an Epic and immediately adds the provided initial stories
     * as child tasks under the Epic. All operations are performed atomically - if any
     * story creation fails, the entire operation is rolled back.</p>
     *
     * <p><strong>Initial Stories Source:</strong> The initial stories are obtained from
     * {@code request.getInitialStories()}. If this field is null or empty, only the
     * Epic will be created without any child stories.</p>
     *
     * <p><strong>Story Configuration:</strong> Each story in the initial list will:
     * <ul>
     *   <li>Have its {@code parentId} automatically set to the created Epic's ID</li>
     *   <li>Have its {@code taskType} automatically set to {@code STORY}</li>
     *   <li>Inherit the Epic's workspace and category if not specified</li>
     *   <li>Be assigned sort orders automatically</li>
     * </ul></p>
     *
     * <h3>Usage Example:</h3>
     * <pre>{@code
     * TaskCreateRequest epicRequest = new TaskCreateRequest();
     * epicRequest.setTitle("E-commerce Platform");
     * epicRequest.setDescription("Complete online shopping platform");
     *
     * // Define initial stories
     * List<TaskCreateRequest> stories = Arrays.asList(
     *     TaskCreateRequest.builder().title("User Registration").build(),
     *     TaskCreateRequest.builder().title("Product Catalog").build(),
     *     TaskCreateRequest.builder().title("Shopping Cart").build()
     * );
     * epicRequest.setInitialStories(stories);
     *
     * Task epic = epicCreationService.createEpicWithInitialStories(userId, epicRequest);
     * }</pre>
     *
     * @param userId the ID of the user creating the Epic and stories
     * @param request the Epic creation request containing Epic details and initial stories
     *                in the {@code initialStories} field
     * @return the created Epic task (child stories are created but not returned)
     * @throws UserNotFoundException if the specified user does not exist
     * @throws TaskValidationException if the Epic request or any story request is invalid
     * @throws TaskBusinessException if database operation fails
     * @throws InvalidTaskTypeException if hierarchy validation fails
     * @since 1.0.0
     * @see BaseTaskCreateRequest getInitialStories()
     * @see createEpicTask(Long, EpicWithStoriesRequest )
     */
    Task createEpicWithInitialStories(Long userId, EpicWithStoriesRequest request);
}
