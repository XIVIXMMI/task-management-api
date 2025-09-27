package com.omori.taskmanagement.service.task.creation;

import com.omori.taskmanagement.dto.project.task.creation.BaseTaskCreateRequest;
import com.omori.taskmanagement.exceptions.task.InvalidTaskTypeException;
import com.omori.taskmanagement.exceptions.task.TaskNotFoundException;
import com.omori.taskmanagement.exceptions.task.TaskValidationException;
import com.omori.taskmanagement.model.project.Task;
import com.omori.taskmanagement.repository.project.TaskRepository;
import com.omori.taskmanagement.service.task.hierarchy.TaskHierarchyValidationService;
import com.omori.taskmanagement.service.task.utils.TaskValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class TaskCreationServiceImpl implements TaskCreationService{

    private final TaskRepository taskRepository;

    private final BaseCreationService baseCreationService;
    private final TaskValidationService taskValidationService;
    private final TaskHierarchyValidationService taskHierarchyValidationService;

    @Override
    public Task createTaskUnderStory(Long userId, Task.TaskType type, BaseTaskCreateRequest request) {
        log.info("Creating task under story for user: {}", userId);
        if (request.getParentId() == null) {
            throw new TaskValidationException("Parent Story ID is required for task creation under story");
        }
        Task parentTask = taskRepository.findById(request.getParentId())
                .orElseThrow(() -> new TaskNotFoundException("Parent task not found with ID: " + request.getParentId()));
        if (parentTask.getTaskType() != Task.TaskType.STORY) {
            throw new InvalidTaskTypeException("TASK can only be created under STORY parent, found: " + parentTask.getTaskType());
        }
        return baseCreationService.createTask(userId, type, request, false);
    }

    @Override
    public Task createStandaloneTask(Long userId, BaseTaskCreateRequest request) {
        log.info("Creating standalone task for user: {}", userId);
        taskValidationService.validateNoParentIdAllowed(request, "standalone task" );
        return baseCreationService.createTask(userId, Task.TaskType.TASK, request, true);
    }

}
