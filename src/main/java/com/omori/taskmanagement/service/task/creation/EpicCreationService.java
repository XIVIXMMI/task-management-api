package com.omori.taskmanagement.service.task.creation;

import com.omori.taskmanagement.dto.project.TaskCreateRequest;
import com.omori.taskmanagement.model.project.Task;

public interface EpicCreationService {

    Task createEpicTask(Long userId, Task.TaskType type, TaskCreateRequest request);
    Task createEpicWithInitialStories(Long userId, Task.TaskType type, TaskCreateRequest request);
}
