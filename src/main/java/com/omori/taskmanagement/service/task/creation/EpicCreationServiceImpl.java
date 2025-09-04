package com.omori.taskmanagement.service.task.creation;

import com.omori.taskmanagement.dto.project.task.InitialStoryRequest;
import com.omori.taskmanagement.dto.project.task.TaskCreateRequest;
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

    private final Task.TaskType EPIC_TYPE = Task.TaskType.EPIC;
    private final Task.TaskType STORY_TYPE = Task.TaskType.STORY;

    @Override
    public Task createEpicTask(Long userId, TaskCreateRequest request) {
        log.info("Creating epic task for user: {}", userId);
        return baseCreationService.createTask(userId , EPIC_TYPE, request);
    }

    @Override
    @Transactional
    public Task createEpicWithInitialStories(Long userId, TaskCreateRequest request) {
        log.info("Creating epic task for user: {} with initial stories", userId);
        Task epicTask = baseCreationService.createTask(userId, EPIC_TYPE, request);
        List<InitialStoryRequest> initialStories = request.getInitialStories();

        if(initialStories != null && !initialStories.isEmpty()){
            for(InitialStoryRequest story : initialStories){
                // Convert to TaskCreateRequest
                TaskCreateRequest fullStoryRequest = story.toTaskCreateRequest();
                // Set and get inherit from this epic task
                fullStoryRequest.setParentId(epicTask.getId());
                if( fullStoryRequest.getCategoryId() == null ){
                    fullStoryRequest.setCategoryId(epicTask.getCategory().getId());
                }
                if( fullStoryRequest.getWorkspaceId() == null ){
                    fullStoryRequest.setWorkspaceId(epicTask.getWorkspace().getId());
                }
                if( fullStoryRequest.getAssignedToId() == null ){
                    fullStoryRequest.setAssignedToId(epicTask.getAssignedTo().getId());
                }
                baseCreationService.createTask(userId, STORY_TYPE, fullStoryRequest);
            }
        }

        return epicTask;
    }
}
