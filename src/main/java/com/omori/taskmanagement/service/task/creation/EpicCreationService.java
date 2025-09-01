package com.omori.taskmanagement.service.task.creation;

import com.omori.taskmanagement.dto.project.TaskCreateRequest;
import com.omori.taskmanagement.model.project.Task;

public interface EpicCreationService {

    Task createEpicTask(Long userId, TaskCreateRequest request);

    /**
     *  Purpose: Create an Epic and immediately add some initial Story tasks under it in one operation.
     *  Use Case: When a user creates an Epic like "E-commerce Website" and wants to immediately add stories like "User Registration", "Product Catalog", "Shopping Cart".
     */
    Task createEpicWithInitialStories(Long userId, Task.TaskType type, TaskCreateRequest request);
}
