package com.omori.taskmanagement.service.task.creation;

import com.omori.taskmanagement.dto.project.TaskCreateRequest;
import com.omori.taskmanagement.model.project.Task;

public interface BaseCreationService {

    Task createTask(Long userId, Task.TaskType type, TaskCreateRequest request);
}
