package com.omori.taskmanagement.service.task;

import com.omori.taskmanagement.dto.project.task.TaskResponse;
import com.omori.taskmanagement.dto.project.task.TaskUpdateRequest;

public interface TaskUpdateService {

    TaskResponse updateTask(Long taskId, Long userId, TaskUpdateRequest request);

}
