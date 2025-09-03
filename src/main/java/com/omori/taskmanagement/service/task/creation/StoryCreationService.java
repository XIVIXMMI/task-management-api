package com.omori.taskmanagement.service.task.creation;

import com.omori.taskmanagement.dto.project.TaskCreateRequest;
import com.omori.taskmanagement.model.project.Task;

public interface StoryCreationService {

    Task createStoryTask(Long userId, TaskCreateRequest request);
    /**
     * Purpose: Create a Story task that belongs to an existing Epic.
     * Use Case: When a user is viewing an Epic and wants to add a new Story to it.
     */

    Task createStoryUnderEpic(Long userId, Task.TaskType type, TaskCreateRequest request);
}
