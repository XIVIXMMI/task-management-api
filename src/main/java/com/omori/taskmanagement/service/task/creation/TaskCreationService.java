package com.omori.taskmanagement.service.task.creation;

import com.omori.taskmanagement.dto.project.TaskCreateRequest;
import com.omori.taskmanagement.model.project.Task;

public interface TaskCreationService {

    Task createTaskUnderStory(Long userId, Task.TaskType type, TaskCreateRequest request);
    Task createStandaloneTask(Long userId, TaskCreateRequest request);
}
