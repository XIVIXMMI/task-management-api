package com.omori.taskmanagement.springboot.service;

import com.omori.taskmanagement.springboot.dto.project.*;
import com.omori.taskmanagement.springboot.exceptions.task.InvalidTaskTypeException;
import com.omori.taskmanagement.springboot.exceptions.task.TaskBusinessException;
import com.omori.taskmanagement.springboot.exceptions.UserNotFoundException;
import com.omori.taskmanagement.springboot.exceptions.task.TaskNotFoundException;
import com.omori.taskmanagement.springboot.exceptions.task.TaskValidationException;
import com.omori.taskmanagement.springboot.model.project.Category;
import com.omori.taskmanagement.springboot.model.project.Subtask;
import com.omori.taskmanagement.springboot.model.project.Task;
import com.omori.taskmanagement.springboot.model.project.Workspace;
import com.omori.taskmanagement.springboot.model.usermgmt.User;
import com.omori.taskmanagement.springboot.repository.project.CategoryRepository;
import com.omori.taskmanagement.springboot.repository.project.TaskRepository;
import com.omori.taskmanagement.springboot.repository.project.WorkspaceRepository;
import com.omori.taskmanagement.springboot.repository.usermgmt.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class TaskHybridServiceImpl implements TaskHybridService {

    private final UserRepository userRepository;
    private final TaskValidationService taskValidationService;
    private final CategoryRepository categoryRepository;
    private final WorkspaceRepository workspaceRepository;
    private final TaskRepository taskRepository;

    private final SubTaskService subTaskService;

    private static final Double DEFAULT_ACTUAL_HOURS = 0.0;
    private static final Integer DEFAULT_PROGRESS = 0;
    private static final Integer DEFAULT_SORT_ORDER = 0;

    @Override
    public Task createEpicTask(Long userId, Task.TaskType type, TaskCreateRequest request) {
        log.debug("Creating epic task for user {} ", userId);

        // Validate that type is actually EPIC
        if (type != Task.TaskType.EPIC) {
            throw new InvalidTaskTypeException("Expected EPIC type, but received: " + type);
        }

        return createHierarchicalTask(userId, type, request);
    }

    @Override
    public Task createStoryTask(Long userId, Task.TaskType type, TaskCreateRequest request) {
        log.debug("Creating story task for user {} ", userId);

        // Validate that type is actually STORY
        if (type != Task.TaskType.STORY) {
            throw new InvalidTaskTypeException("Expected STORY type, but received: " + type);
        }

        return createHierarchicalTask(userId, type, request);
    }

    @Override
    public Task addSubtasksToTask(Long taskId, List<String> subtasksTitles) {
        log.debug("Adding subtasks to task with ID {} ", taskId);

        Task task = taskRepository.findByIdWithRelations(taskId)
                .orElseThrow(() -> new TaskBusinessException("Task not found with ID: " + taskId));

        for (String subtaskTitle : subtasksTitles) {
            if (subtaskTitle == null || subtaskTitle.isBlank()) {
                throw new TaskValidationException("All subtask titles must be non-empty",
                        Map.of("invalidTitle", "Found null or empty title"));
            }
            subTaskService.createSubtask(null, taskId, subtaskTitle.trim());
        }

        return task;
    }

    @Override
    public List<Task> getStoriesTaskByEpicId(Long epicTaskId){
        log.debug("Getting all stories tasks for epic task with ID {} ", epicTaskId);
        return taskRepository
                .findByParentTaskIdAndTaskTypeAndDeletedAtIsNull(epicTaskId, Task.TaskType.STORY);
    }

    @Override
    public List<Task> getTasksByStoryId(Long storyTaskId){
        log.debug("Getting all tasks for story task with ID {} ", storyTaskId);
        return taskRepository
                .findByParentTaskIdAndTaskTypeAndDeletedAtIsNull(storyTaskId, Task.TaskType.TASK);
    }

    @Override
    @Transactional
    public HierarchyEpicDto getFullHierarchy(Long epicId){
        log.debug("Getting full hierarchy for epic task with ID {} ", epicId);
        // Validate task existed and is EPIC task 
        Task epic = taskRepository.findByIdWithRelations(epicId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with ID: " + epicId));

        if( epic.getTaskType() != Task.TaskType.EPIC){
            throw new TaskValidationException("Task with id + " + epicId + " is not an EPIC Task",
                Map.of("taskType","Excepted EPIC but found " + epic.getTaskType()));

        }

        List<Task> stories = getStoriesTaskByEpicId(epicId);

        HierarchyEpicDto hierarchy = new HierarchyEpicDto();
        hierarchy.setEpic(TaskResponse.from(epic));

        for( Task story : stories ) {
            StoryWithTaskDto storyWithTaskDto = new StoryWithTaskDto();
            storyWithTaskDto.setStory(TaskResponse.from(story));
            storyWithTaskDto.setTasks(getTasksByStoryId(story.getId()));

            // load subtask for each task
            for(Task task : storyWithTaskDto.getTasks()) {
                List<Subtask> subtasks = subTaskService.getSubtasksByTaskId(task.getId());
                task.setSubtasks(subtasks);
            }
            hierarchy.getStories().add(storyWithTaskDto);
        }
        return hierarchy;
    }

    @Override
    @Transactional
    public void updateEpicTaskProgress(Long epicTaskId) {
        Task epic = taskRepository.findById(epicTaskId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with ID: " + epicTaskId));
        if( epic.getTaskType() != Task.TaskType.EPIC){
            throw new TaskValidationException("Task with id + " + epicTaskId + " is not an EPIC Task",
                    Map.of("taskType","Excepted EPIC but found " + epic.getTaskType()));
        }
        List<Task> stories = getStoriesTaskByEpicId(epicTaskId);
        if( stories.isEmpty()) {
            log.debug("No stories found for epic task with ID {} ", epicTaskId);
            return;
        }
        double avgProgress = stories.stream()
                .mapToInt(t -> Optional.ofNullable(t.getProgress()).orElse(0) )
                .average()
                .orElse(0.0);
        epic.setProgress((int) Math.round( avgProgress));
        epic.setUpdatedAt(LocalDateTime.now());
        taskRepository.save(epic);
        log.debug("Updated progress for epic task with ID {} ", epicTaskId);
    }

    @Override
    @Transactional
    public void updateStoryTaskProgress(Long storyTaskId){
        Task story = taskRepository.findByIdWithRelations(storyTaskId)
                .orElseThrow(() -> new TaskBusinessException("Task not found with ID: " + storyTaskId));
        List<Task> tasks = getTasksByStoryId(storyTaskId);
        if( tasks.isEmpty()) {
            log.debug("No tasks found for story task with ID {} ", storyTaskId);
            return;
        }
        double avgProgress = tasks.stream()
                .mapToInt(Task::getProgress)
                .average()
                .orElse(0.0);
        story.setProgress((int) Math.round( avgProgress));
        story.setUpdatedAt(LocalDateTime.now());
        taskRepository.save(story);
        if(story.getParentTask() != null && story.getParentTask().getId() != null ) {
            updateEpicTaskProgress(story.getParentTask().getId());
        }
        log.debug("Updated progress for story task with ID {} ", storyTaskId);
    }

    /**
     * =========UTILITIES========
     * */

    @Transactional
    private Task createHierarchicalTask(Long userId, Task.TaskType type, TaskCreateRequest request) {
        log.debug("Creating hierarchical task for user {} ", userId);

        taskValidationService.validateCreateTaskRequest(request, userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User " + userId + " not found"));

        Task epicTask = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .dueDate(request.getDueDate())
                .startDate(request.getStartDate())
                .priority(request.getPriority())
                .status(Task.TaskStatus.pending)
                .taskType(type)
                .estimatedHours(request.getEstimatedHours())
                .actualHours(DEFAULT_ACTUAL_HOURS)
                .progress(DEFAULT_PROGRESS)
                .user(user)
                .sortOrder(Optional.ofNullable(request.getSortOrder()).orElse(DEFAULT_SORT_ORDER))
                .isRecurring(Optional.ofNullable(request.getIsRecurring()).orElse(false))
                .recurrencePattern(request.getRecurrencePattern())
                .metadata(request.getMetadata())
                .build();

        setTaskRelations(epicTask,
                request.getCategoryId(),
                request.getAssignedToId(),
                request.getWorkspaceId());

        // Set parent task if creating STORY or EPIC
        if( request.getParentId() != null){
            Task parentTask = taskRepository.findById(request.getParentId())
                    .orElseThrow(() -> new TaskNotFoundException("Parent task not found with ID: "
                            + request.getParentId()));

            // Validate hierarchy
            if(!parentTask.getTaskType().canContain(type)){
                throw new InvalidTaskTypeException(
                        String.format("%s cannot contain %s", parentTask.getTaskType(), type)
                );
            }
            epicTask.setParentTask(parentTask);
            epicTask.setSortOrder(getNextSortOrderForParent(request.getParentId()).intValue());
        }


        try {
            Task savedEpicTask = taskRepository.save(epicTask);
            log.debug("Epic task created successfully with ID {} for user {}",
                    savedEpicTask.getId(), userId);
            return savedEpicTask;
        } catch (DataAccessException e) {
            log.error("Failed to save epic task for user {}: {}", userId, e.getMessage());
            throw new TaskBusinessException("Failed to create epic task", e);
        }
    }

    private Long getNextSortOrderForParent(Long parentTaskId) {
        // Implementation to get next sort order
        return taskRepository.findMaxSortOrderByParentTaskId(parentTaskId)
                .map(max -> max + 1)
                .orElse(0L);
    }

    // Helper method to set task relations
    private void setTaskRelations(Task task, Long categoryId, Long assignedToId, Long workspaceId) {
        if (categoryId != null) {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new TaskBusinessException("Category not found with id: " + categoryId));
            task.setCategory(category);
        }

        if (assignedToId != null) {
            User assignedTo = userRepository.findById(assignedToId)
                    .orElseThrow(() -> new UserNotFoundException("User not found with id: " + assignedToId));
            task.setAssignedTo(assignedTo);
        }

        if (workspaceId != null) {
            Workspace workspace = workspaceRepository.findById(workspaceId)
                    .orElseThrow(() -> new TaskBusinessException("Workspace not found with id: " + workspaceId));
            task.setWorkspace(workspace);
        }
    }
}
