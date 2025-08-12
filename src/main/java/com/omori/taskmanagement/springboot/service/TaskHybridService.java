package com.omori.taskmanagement.springboot.service;

import com.omori.taskmanagement.springboot.dto.project.TaskCreateRequest;
import com.omori.taskmanagement.springboot.model.project.Task;

public interface TaskHybridService {

    /**
     * Create and persists tasks for a specific type using provided data
     *
     * @param type      the type of task to create (e,g ... EPIC, STORY, TASK)
     * @param request   container for all required fields
     * @return the persisted Task entity
     */
    Task createEpicTask(Task.TaskType type, TaskCreateRequest request);
    Task createStoryTask();



    

}