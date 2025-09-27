package com.omori.taskmanagement.service.task.creation;

import com.omori.taskmanagement.dto.project.task.creation.EpicCreateRequest;
import com.omori.taskmanagement.dto.project.task.creation.EpicWithStoriesRequest;
import com.omori.taskmanagement.dto.project.task.creation.InitialStoryRequest;
import com.omori.taskmanagement.dto.project.task.creation.BaseTaskCreateRequest;
import com.omori.taskmanagement.exceptions.task.TaskValidationException;
import com.omori.taskmanagement.model.project.Task;
import com.omori.taskmanagement.service.task.utils.TaskValidationService;
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
    private final TaskValidationService validationService;

    private final Task.TaskType EPIC_TYPE = Task.TaskType.EPIC;
    private final Task.TaskType STORY_TYPE = Task.TaskType.STORY;

    @Override
    public Task createEpicTask(Long userId, EpicCreateRequest request) {
        log.info("Creating epic task for user: {}", userId);
        if(request.getParentId() != null){
            throw new TaskValidationException("Epic Task cannot have a parent task ID");

        }
        return baseCreationService.createTask(userId , EPIC_TYPE, request,true);
    }

    @Override
    @Transactional
    public Task createEpicWithInitialStories(Long userId, EpicWithStoriesRequest request) {
        log.info("Creating epic task for user: {} with initial stories", userId);
        if(request.getParentId() != null){
            throw new TaskValidationException("Epic Task cannot have a parent task ID");
        }
        Task epicTask = baseCreationService.createTask(userId, EPIC_TYPE, request, true);
        List<InitialStoryRequest> initialStories = request.getInitialStories();
        if(initialStories != null && !initialStories.isEmpty()){
            for(InitialStoryRequest story : initialStories){
                // Convert to TaskCreateRequest
                BaseTaskCreateRequest fullStoryRequest = story.toTaskCreateRequest();
                // Set and get inherit from this epic task
                fullStoryRequest.setParentId(epicTask.getId());
                if( fullStoryRequest.getCategoryId() == null && epicTask.getCategory() != null){
                    fullStoryRequest.setCategoryId(epicTask.getCategory().getId());
                }
                if( fullStoryRequest.getWorkspaceId() == null && epicTask.getWorkspace() != null){
                    fullStoryRequest.setWorkspaceId(epicTask.getWorkspace().getId());
                }
                baseCreationService.createTask(userId, STORY_TYPE, fullStoryRequest, false);
            }
        }
        return epicTask;
    }
}
