package com.omori.taskmanagement.service.task.creation;

import com.omori.taskmanagement.dto.project.task.TaskCreateRequest;
import com.omori.taskmanagement.model.project.Task;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class TaskCreationServiceImpl implements TaskCreationService{

    private final BaseCreationServiceImpl baseCreationServiceImpl;

    @Override
    public Task createTaskUnderStory(Long userId, Task.TaskType type, TaskCreateRequest request) {
        log.info("Creating task under story for user: {}", userId);
        return baseCreationServiceImpl.createTask(userId, type, request);
    }

    @Override
    public Task createStandaloneTask(Long userId, TaskCreateRequest request) {
        log.info("Creating standalone task for user: {}", userId);
        return baseCreationServiceImpl.createTask(userId, Task.TaskType.TASK, request);
    }

}
