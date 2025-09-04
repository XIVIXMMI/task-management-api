package com.omori.taskmanagement.service.task.creation;

import com.omori.taskmanagement.dto.project.task.TaskCreateRequest;
import com.omori.taskmanagement.exceptions.UserNotFoundException;
import com.omori.taskmanagement.exceptions.task.InvalidTaskTypeException;
import com.omori.taskmanagement.exceptions.task.TaskBusinessException;
import com.omori.taskmanagement.exceptions.task.TaskValidationException;
import com.omori.taskmanagement.model.project.Task;

public interface EpicCreationService {

    /**
     * Creates an Epic task. Task type is implicitly set to EPIC.
     *
     * @param userId the user creating the epic
     * @param request the epic creation request
     * @return created Epic task
     */
    Task createEpicTask(Long userId, TaskCreateRequest request);

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
     * @see TaskCreateRequest#getInitialStories()
     * @see #createEpicTask(Long, TaskCreateRequest)
     */
    Task createEpicWithInitialStories(Long userId, TaskCreateRequest request);
}
