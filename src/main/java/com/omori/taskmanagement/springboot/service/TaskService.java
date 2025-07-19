package com.omori.taskmanagement.springboot.service;

import com.omori.taskmanagement.springboot.dto.project.CreateTaskRequest;
import com.omori.taskmanagement.springboot.dto.project.CreateTaskResponse;
import com.omori.taskmanagement.springboot.model.project.Task;

public interface TaskService {
    Task createTask(Long id, CreateTaskRequest request);
}
