package com.omori.taskmanagement.service.task.utils;

import com.omori.taskmanagement.model.project.Task;

public interface TaskAccessControlService {

    void validateTaskAccess(Task task, Long userId);
}
