package com.omori.taskmanagement.springboot.service;

import com.omori.taskmanagement.springboot.model.project.Task;

public interface TaskHybridService {

    Task createEpicTask();

    Task createStoryTask();

    

}