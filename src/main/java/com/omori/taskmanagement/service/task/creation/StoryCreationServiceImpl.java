package com.omori.taskmanagement.service.task.creation;

import com.omori.taskmanagement.dto.project.task.creation.BaseTaskCreateRequest;
import com.omori.taskmanagement.exceptions.task.InvalidTaskTypeException;
import com.omori.taskmanagement.exceptions.task.TaskNotFoundException;
import com.omori.taskmanagement.exceptions.task.TaskValidationException;
import com.omori.taskmanagement.model.project.Task;
import com.omori.taskmanagement.repository.project.TaskRepository;
import com.omori.taskmanagement.service.task.utils.TaskValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class StoryCreationServiceImpl implements StoryCreationService{

    private final BaseCreationService baseCreationService;
    private final TaskRepository taskRepository;
    private final TaskValidationService taskValidationService;

    @Override
    public Task createStoryTask(Long userId, BaseTaskCreateRequest request) {
        log.info("Creating story task for user: {}", userId);
        taskValidationService.validateNoParentIdAllowed(request, "standalone story" );
        return baseCreationService.createTask(userId, Task.TaskType.STORY, request, true);
    }

    @Override
    public Task createStoryUnderEpic(Long userId, Task.TaskType type, BaseTaskCreateRequest request) {
        log.info("Creating story under epic for user: {}", userId);

        if (request.getParentId() == null) {
            throw new TaskValidationException("Parent Epic ID is required for story creation");
        }

        Task parentEpic = taskRepository.findById(request.getParentId())
                .orElseThrow(() -> new TaskNotFoundException("Parent Epic not found"));

        if (parentEpic.getTaskType() != Task.TaskType.EPIC) {
            throw new InvalidTaskTypeException("Parent must be an EPIC task");
        }

        return baseCreationService.createTask(userId, Task.TaskType.STORY, request, false);
    }
}
