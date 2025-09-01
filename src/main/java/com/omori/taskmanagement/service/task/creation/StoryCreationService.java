package com.omori.taskmanagement.service.task.creation;

import com.omori.taskmanagement.dto.project.TaskCreateRequest;
import com.omori.taskmanagement.model.project.Task;

public interface StoryCreationService {

    Task createStoryTask(Long userId, Task.TaskType type, TaskCreateRequest request);
    Task createStoryUnderEpic(Long userId, Task.TaskType type, TaskCreateRequest request);
}
