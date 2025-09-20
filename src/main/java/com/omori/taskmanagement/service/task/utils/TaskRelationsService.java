package com.omori.taskmanagement.service.task.utils;

import com.omori.taskmanagement.model.project.Task;

public interface TaskRelationsService {
    void setTaskRelations(Task task, Long categoryId, Long assignedToId, Long workspaceId);

}
