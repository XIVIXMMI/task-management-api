package com.omori.taskmanagement.service.task;

import com.omori.taskmanagement.model.project.Task;

public interface TaskAccessControlService {

    void validateTaskAccess(Task task, Long userId);
}
