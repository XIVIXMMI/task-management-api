package com.omori.taskmanagement.service.task.creation;

import com.omori.taskmanagement.dto.project.TaskCreateRequest;
import com.omori.taskmanagement.model.project.Task;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EpicCreationServiceImpl implements EpicCreationService{

    private final BaseCreationService baseCreationService;

    @Override
    public Task createEpicTask(Long userId, TaskCreateRequest request) {
        log.info("Creating epic task for user: {}", userId);
        return baseCreationService.createTask(userId, Task.TaskType.EPIC, request);
    }

    @Override
    @Transactional
    public Task createEpicWithInitialStories(Long userId, Task.TaskType type, TaskCreateRequest request) {
        log.info("Creating epic task for user: {} with initial stories", userId);
        Task epicTask = baseCreationService.createTask(userId, type, request);
        List<TaskCreateRequest> initialStories = request.getInitialStories();

        if(initialStories != null && !initialStories.isEmpty()){
            for(TaskCreateRequest story : initialStories){
                story.setParentId(epicTask.getId());
                baseCreationService.createTask(userId, Task.TaskType.STORY, story);
            }
        }

        return epicTask;
    }
}
