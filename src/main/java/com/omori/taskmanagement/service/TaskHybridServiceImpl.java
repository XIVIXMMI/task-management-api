package com.omori.taskmanagement.service;

import com.omori.taskmanagement.dto.project.*;
import com.omori.taskmanagement.exceptions.task.InvalidTaskTypeException;
import com.omori.taskmanagement.exceptions.task.TaskBusinessException;
import com.omori.taskmanagement.exceptions.UserNotFoundException;
import com.omori.taskmanagement.exceptions.task.TaskNotFoundException;
import com.omori.taskmanagement.exceptions.task.TaskValidationException;
import com.omori.taskmanagement.model.project.Category;
import com.omori.taskmanagement.model.project.Subtask;
import com.omori.taskmanagement.model.project.Task;
import com.omori.taskmanagement.model.project.Workspace;
import com.omori.taskmanagement.model.usermgmt.User;
import com.omori.taskmanagement.repository.project.CategoryRepository;
import com.omori.taskmanagement.repository.project.SubtaskRepository;
import com.omori.taskmanagement.repository.project.TaskRepository;
import com.omori.taskmanagement.repository.project.WorkspaceRepository;
import com.omori.taskmanagement.repository.usermgmt.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.omori.taskmanagement.model.events.TaskProgressUpdateEvent;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class TaskHybridServiceImpl implements TaskHybridService {

    private final UserRepository userRepository;
    private final TaskValidationService taskValidationService;
    private final CategoryRepository categoryRepository;
    private final WorkspaceRepository workspaceRepository;
    private final TaskRepository taskRepository;
    private final SubtaskRepository subtaskRepository;

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
    public Task createTask(Long userId, Task.TaskType type, TaskCreateRequest request) {
        log.debug("Creating task for user {} ", userId);

        // Validate that type is actually TASK
        if (type != Task.TaskType.TASK) {
            throw new InvalidTaskTypeException("Expected TASK type, but received: " + type);
        }

        return createHierarchicalTask(userId, type, request);
    }

    @Override
    public List<Subtask> addSubtasksToTask(Long taskId, List<String> subtasksTitles) {
        log.debug("Adding subtasks to task with ID {} ", taskId);

        Task task = taskRepository.findByIdWithRelations(taskId)
                .orElseThrow(() -> new TaskBusinessException("Task not found with ID: " + taskId));

        List<Subtask> subtasks = new ArrayList<>();

        for (String subtaskTitle : subtasksTitles) {
            if (subtaskTitle == null || subtaskTitle.isBlank()) {
                throw new TaskValidationException("All subtask titles must be non-empty",
                        Map.of("invalidTitle", "Found null or empty title"));
            }
            Subtask subtask = subTaskService.createSubtask(
                    SubtaskRequest.builder()
                            .taskId(taskId)
                            .title(subtaskTitle)
                    .build()
            );
            subtasks.add(subtask);
        }

        return subtasks;
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
    @Transactional(readOnly = true)
    public HierarchyEpicDto getFullHierarchy(Long epicId){
        log.debug("Getting full hierarchy for epic task with ID {} ", epicId);

        // Single query to get all tasks in hierarchy
        List<Task> allTasks = taskRepository.findAllTasksUnderEpic(epicId);

        Task epic = allTasks.stream()
                .filter( t -> t.getId().equals(epicId))
                .findFirst()
                .orElseThrow(() -> new TaskNotFoundException("Task not found with ID: " + epicId));

        if (epic.getTaskType() != Task.TaskType.EPIC) {
            throw new TaskValidationException("Task with id " + epicId + " is not an EPIC task",
                    Map.of("taskType", "Expected EPIC but found " + epic.getTaskType()));
        }

        // Group by parent efficiently
        Map<Long, List<Task>> tasksByParent = allTasks.stream()
                .filter(t -> t.getParentTask() != null)
                .collect(Collectors.groupingBy( t -> t.getParentTask().getId()));

        // Build hierarchy in one pass
        HierarchyEpicDto hierarchy = new HierarchyEpicDto();
        hierarchy.setEpic(TaskResponse.from(epic));

        List<Task> stories = tasksByParent.getOrDefault(epicId, Collections.emptyList());

        for( Task story : stories ) {
            StoryWithTaskDto storyWithTaskDto = new StoryWithTaskDto();
            storyWithTaskDto.setStory(TaskResponse.from(story)); // Convert Task to TaskResponse
            List<TaskResponse> taskResponses = tasksByParent.getOrDefault(story.getId(), Collections.emptyList())
                    .stream()
                    .map(TaskResponse::from)
                    .toList();
            storyWithTaskDto.setTasks(taskResponses);
            hierarchy.getStories().add(storyWithTaskDto);
        }
        if (!hierarchy.getStories().isEmpty()) {
            loadSubtasksForHierarchy(hierarchy);
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

    @Override
    @Transactional
    public void updateTaskProgressFromSubtasks(Long taskId) {
        log.debug("Updating progress from subtask for task with ID {} ", taskId);

        Task task = taskRepository.findByIdWithRelations(taskId)
                .orElseThrow(() -> new TaskBusinessException("Task not found with ID: " + taskId));

        List<Subtask> subtasks = subTaskService.getSubtasksByTaskId(taskId);
        if (subtasks.isEmpty()){
            log.debug("No subtasks found for task with ID {} ", taskId);
            return;
        }

        // calculate progress based on subtasks completed
        long completedCount = subtasks.stream()
                .mapToLong(s -> Boolean.TRUE.equals(s.getIsCompleted()) ? 1 : 0)
                .sum();

        int progressPercent = (int) ((completedCount * 100) / subtasks.size());
        log.debug("Task ID: {} has {}/{} subtasks completed, setting progress to {}%",
                taskId, completedCount, subtasks.size(), progressPercent);

        // Update task progress based on subtask completion
        task.setProgress(progressPercent);
        task.setUpdatedAt(LocalDateTime.now());

        try {
            taskRepository.save(task);
            log.debug("Successfully updated progress for task with ID {} ", taskId);
        } catch (DataAccessException e) {
            log.error("Failed to update progress for task with ID {}: {}", taskId, e.getMessage());
            throw new TaskBusinessException("Failed to update progress", e);
        }


        // Trigger cascade update to parent task
        if (task.getParentTask() != null && task.getParentTask().getId() != null) {
            Long parentId = task.getParentTask().getId();
            Task.TaskType parentType = task.getParentTask().getTaskType();

            log.debug("Triggering update to parent task with ID {} of type {}", taskId, parentId);

            if (parentType == Task.TaskType.STORY) {
                updateStoryTaskProgress(parentId);
            } else if (parentType == Task.TaskType.EPIC) {
                updateEpicTaskProgress(parentId);
            }

            log.debug("Successfully triggered update to parent task with ID {} of type {}", taskId, parentType);
        }
    }
    
    /**
     * Event listener that handles task progress updates triggered by subtask changes.
     * This breaks the circular dependency between SubTaskService and TaskHybridService.
     */
    @EventListener
    @Transactional
    public void handleTaskProgressUpdateEvent(TaskProgressUpdateEvent event) {
        log.debug("Handling task progress update event for task ID: {} - Reason: {}", 
                event.getTaskId(), event.getReason());
        updateTaskProgressFromSubtasks(event.getTaskId());
    }

    /**
     * =========UTILITIES========
     **/

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

        // Set a parent task if creating STORY or EPIC
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

    private void loadSubtasksForHierarchy(HierarchyEpicDto hierarchy) {
        // Collect ALL task IDs: EPIC + STORY + TASK
        List<Long> allTaskIds = new ArrayList<>();

        // Add EPIC id
        allTaskIds.add(hierarchy.getEpic().getId());

        // Add STORY + TASK IDs
        hierarchy.getStories().forEach( story -> {
                allTaskIds.add(story.getStory().getId()); // STORY subtasks
                story.getTasks().forEach(task -> allTaskIds.add(task.getId()));
                });

        // Bulk load subtasks for ALL levels
        List<Subtask> allSubtasks = subtaskRepository.findByTaskIdInAndDeletedAtIsNull(allTaskIds);
        Map<Long, List<Subtask>> subtasksByTaskId = allSubtasks.stream()
                .collect(Collectors.groupingBy(s -> s.getTask().getId()));

        // Assign subtasks to EPIC
        hierarchy.getEpic().setSubtasks(
                subtasksByTaskId.getOrDefault(hierarchy.getEpic().getId(), Collections.emptyList())
                        .stream()
                        .map(SubtaskResponse::from)
                        .collect(Collectors.toList())
        );

        // Assign subtasks to STORY and TASK
        hierarchy.getStories().forEach( story -> {
            // Assign to STORY
            story.getStory().setSubtasks(
                    subtasksByTaskId.getOrDefault(story.getStory().getId(), Collections.emptyList())
                            .stream()
                            .map(SubtaskResponse::from)
                            .collect(Collectors.toList())
            );
            // Assign to TASK
            story.getTasks().forEach( task -> {
                task.setSubtasks(
                        subtasksByTaskId.getOrDefault(task.getId(), Collections.emptyList())
                                .stream()
                                .map(SubtaskResponse::from)
                                .collect(Collectors.toList())
                );
            });
        });
        log.debug("Loading subtasks for {} tasks: {}", allTaskIds.size(), allTaskIds);
    }

    private Integer getNextSortOrderForParent(Long parentTaskId) {
        // Implementation to get next sort order
        return taskRepository.findMaxSortOrderByParentTaskId(parentTaskId)
                .map(max -> max + 1)
                .orElse(0);
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
