package com.omori.taskmanagement.service.task.creation;

import com.omori.taskmanagement.dto.project.task.creation.BaseTaskCreateRequest;
import com.omori.taskmanagement.exceptions.UserNotFoundException;
import com.omori.taskmanagement.exceptions.task.InvalidTaskTypeException;
import com.omori.taskmanagement.exceptions.task.TaskValidationException;
import com.omori.taskmanagement.model.project.Task;

public interface BaseCreationService {

    /**
     * Creates a new task with the specified type and configuration.
     *
     * <p><strong>Important:</strong> The method-level {@code type} parameter
     * always takes precedence over {@code request.getType()}. If there is a
     * mismatch between the two values, this method will either normalize the
     * request or throw a validation exception depending on the implementation.</p>
     *
     * <p>This design ensures type safety and prevents inconsistencies between
     * the method contract and the request payload.</p>
     *
     * @param userId the ID of the user creating the task
     * @param type the task type to create (EPIC, STORY, or TASK) - this parameter
     *             overrides any type specified in the request
     * @param request the task creation request containing task details
     * @return the created and persisted Task entity
     * @throws UserNotFoundException if the specified user does not exist
     * @throws InvalidTaskTypeException if type validation fails or if there's
     *                                  a mismatch between method parameter and request type
     * @throws TaskValidationException if the request contains invalid data
     * @since 1.0.0
     */
    Task createTask(Long userId, Task.TaskType type, BaseTaskCreateRequest request, boolean ignoreParent);
}
