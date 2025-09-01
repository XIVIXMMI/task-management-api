package com.omori.taskmanagement.service.task;

import com.omori.taskmanagement.dto.project.TaskResponse;
import com.omori.taskmanagement.dto.project.TaskUpdateRequest;

public interface TaskUpdateService {

    TaskResponse updateTask(Long taskId, Long userId, TaskUpdateRequest request);

}
