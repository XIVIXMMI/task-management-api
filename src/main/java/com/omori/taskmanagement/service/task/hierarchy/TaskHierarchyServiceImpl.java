package com.omori.taskmanagement.service.task.hierarchy;

import com.omori.taskmanagement.dto.project.subtask.SubtaskResponse;
import com.omori.taskmanagement.dto.project.task.HierarchyEpicDto;
import com.omori.taskmanagement.dto.project.task.StoryWithTaskDto;
import com.omori.taskmanagement.dto.project.task.TaskResponse;
import com.omori.taskmanagement.exceptions.task.InvalidTaskTypeException;
import com.omori.taskmanagement.exceptions.task.TaskNotFoundException;
import com.omori.taskmanagement.exceptions.task.TaskValidationException;
import com.omori.taskmanagement.model.project.Subtask;
import com.omori.taskmanagement.model.project.Task;
import com.omori.taskmanagement.repository.project.SubtaskRepository;
import com.omori.taskmanagement.repository.project.TaskRepository;
import com.omori.taskmanagement.service.task.update.TaskProgressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor

public class TaskHierarchyServiceImpl implements TaskHierarchyService{

    private final TaskRepository taskRepository;
    private final SubtaskRepository subtaskRepository;

    private final TaskHierarchyValidationService taskHierarchyValidationService;
    private final TaskProgressService taskProgressService;

    private static final Task.TaskType EPIC = Task.TaskType.EPIC;
    private static final Task.TaskType STORY = Task.TaskType.STORY;
    private static final Task.TaskType TASK = Task.TaskType.TASK;

    @Override
    public HierarchyEpicDto getFullHierarchy(Long epicId) {
        log.debug("Getting full hierarchy for epic task with ID {} ", epicId);

        // Single query to get all tasks in hierarchy
        List<Task> allTasks = taskRepository.findAllTasksUnderEpic(epicId);

        Task epic = allTasks.stream()
                .filter( t -> t.getId().equals(epicId))
                .findFirst()
                .orElseThrow(() -> new TaskNotFoundException("Task not found with ID: " + epicId));

        if (epic.getTaskType() != EPIC) {
            throw new TaskValidationException("Task with id " + epicId + " is not an EPIC task",
                    Map.of("taskType", "Expected EPIC but found " + epic.getTaskType()));
        }

        // Group by parent efficiently
        Map<Long, List<Task>> tasksByParent = allTasks.stream()
                .filter(t -> t.getParentTask() != null)
                .collect(Collectors.groupingBy(t -> t.getParentTask().getId()));

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

        loadSubtasksForHierarchy(hierarchy);
        return hierarchy;
    }

    @Override
    public HierarchyEpicDto getFullHierarchyByUuid(String epicUuid) {
        log.debug("Getting full hierarchy for epic task with Uuid {} ", epicUuid);
        UUID uuid;

        try {
            uuid = UUID.fromString(epicUuid);
        } catch (IllegalArgumentException e) {
            throw new TaskValidationException(
                    "Invalid UUID format: " + epicUuid,
                    Map.of("uuid", "Invalid UUID format: " + epicUuid)
            );
        }
        List<Task> allTasks = taskRepository.findAllTasksUnderEpicByUuid(uuid);
        Task epic = allTasks.stream()
                .filter( t -> t.getUuid().equals(uuid))
                .findFirst()
                .orElseThrow( () -> new TaskNotFoundException("Task not found with uuid: " + epicUuid) );

        if(epic.getTaskType() != EPIC){
            throw new TaskValidationException("Task with id " + epicUuid + " is not an EPIC task",
                    Map.of("taskType","Excepted EPIC but found " + epic.getTaskType()));
        }

        Map<UUID ,List<Task>> tasksByParent = allTasks.stream()
                .filter(t -> t.getParentTask() != null)
                .collect(Collectors.groupingBy( t -> t.getParentTask().getUuid()));

        HierarchyEpicDto hierarchy = new HierarchyEpicDto();
        hierarchy.setEpic(TaskResponse.from(epic));

        List<Task> stories = tasksByParent.getOrDefault(uuid, Collections.emptyList());

        for( Task story : stories ) {
            StoryWithTaskDto storyWithTaskDto = new StoryWithTaskDto();
            storyWithTaskDto.setStory(TaskResponse.from(story)); // Convert Task to TaskResponse
            List<TaskResponse> taskResponses = tasksByParent.getOrDefault(story.getUuid(), Collections.emptyList())
                    .stream()
                    .map(TaskResponse::from)
                    .toList();
            storyWithTaskDto.setTasks(taskResponses);
            hierarchy.getStories().add(storyWithTaskDto);
        }
        loadSubtasksForHierarchy(hierarchy);
        return hierarchy;
    }

    @Override
    public TaskResponse getParentTask(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with ID: " + taskId));
        Long parentId = taskRepository.findParentTaskIdByTaskId(taskId);
        if( parentId == null ) {
            log.debug("Task {} has no parent", taskId);
            return null;
        }
        Task parentTask = taskRepository.findById(parentId).orElse(null);

        return parentTask !=null ? TaskResponse.from(parentTask) : null;
    }

    @Override
    public List<Task> getChildTasks(Long parentTaskId) {
        log.debug("Getting children for task with ID {} ", parentTaskId);
        Task parentTask = taskRepository.findById(parentTaskId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with ID: " + parentTaskId));

        return switch (parentTask.getTaskType()) {
            case EPIC -> taskRepository.findByParentTaskIdAndTaskTypeAndDeletedAtIsNull(parentTaskId, STORY);
            case STORY -> taskRepository.findByParentTaskIdAndTaskTypeAndDeletedAtIsNull(parentTaskId, TASK);
            case TASK -> {
                log.debug("Task {} is the TASK level, no children found (only subtask)", parentTaskId);
                yield Collections.emptyList();
            }
            default -> {
                log.warn("Unexpected task type: {}", parentTask.getTaskType());
                yield Collections.emptyList();
            }
        };
    }

    @Override
    public List<Task> getAllChildTasks(Long parentTaskId) {
        log.debug("Getting all descendant tasks for parent task with ID {}", parentTaskId);
        Task parentTask = taskRepository.findById(parentTaskId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with ID: " + parentTaskId));

        return switch (parentTask.getTaskType()) {
            case EPIC -> {
                List<Task> allTasks = taskRepository.findAllTasksUnderEpic(parentTaskId);
                // Return all descendants, excluding the epic itself
                yield allTasks.stream()
                        .filter(t -> !Objects.equals(t.getId(), parentTaskId))
                        .collect(Collectors.toList());
            }
            case STORY -> {
                yield taskRepository.findByParentTaskIdAndTaskTypeAndDeletedAtIsNull(parentTaskId, TASK);
            }
            case TASK -> {
                log.debug("Id {} is the TASK level, no children found (only subtask)", parentTaskId);
                yield Collections.emptyList();
            }
            default -> {
                log.warn("Unexpected Task type: {}", parentTask.getTaskType());
                yield Collections.emptyList();
            }
        };
    }

    @Override
    @Transactional
    public void moveTaskToParent(Long taskId, Long parentId) {
        log.debug("Moving task with ID {} to parent with ID {}", taskId, parentId);
        Task task = taskRepository.findByIdAndDeletedAtIsNull(taskId)
                .orElseThrow( () -> new TaskNotFoundException("Task not found with ID: " + taskId) );

        Task newParent = null;
        if(parentId != null) {
            newParent = taskRepository.findByIdAndDeletedAtIsNull(parentId)
                    .orElseThrow( () -> new TaskNotFoundException("Task not found with ID: " + parentId) );
        }

        Task oldParent = task.getParentTask();
        taskHierarchyValidationService.validateTaskType(task);
        validateMoveOperation(task,newParent);
        preventInfiniteLoop(taskId, parentId);

        if(parentId != null ) {
            int currentParentDepth = getHierarchyDepth(parentId);
            int taskSubtreeDepth = getHierarchyDepth(taskId);
            // Check if move would exceed depth limit
            if(currentParentDepth + 1 > 3) {
                throw new TaskValidationException("Moving task would exceed maximum allowed depth of 3");
            }
        }

        task.setParentTask(newParent);
        if( newParent != null ) {
            task.setSortOrder(getNextSortOrderForParent(newParent.getId()));
        }
        task.setUpdatedAt(LocalDateTime.now());
        taskRepository.save(task);

        recalculateProgress(task,oldParent,newParent);

        log.info("Successfully moved Task {} from parent {} to parent {}",
                taskId, oldParent != null ? oldParent.getId() : null, parentId);
    }

    @Override
    public int getHierarchyDepth(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with ID: " + taskId));
        int depth = 0;
        final int MAX_DEPTH = 10;
        while(task.getParentTask() != null && depth < MAX_DEPTH) {
            depth++;
            task = task.getParentTask();
        }
        return depth;
    }

    @Override
    public Integer getNextSortOrderForParent(Long parentTaskId) {
        // Implementation to get next sort order
        return taskRepository.findMaxSortOrderByParentTaskId(parentTaskId)
                .map(max -> (Integer) (max + 1))
                .orElse(Integer.valueOf(0));
    }

    /*
    ========== HELPER METHODS ==========
    */

    private void validateMoveOperation(Task task, Task newParent){
        switch (task.getTaskType()) {
            case EPIC -> throw new InvalidTaskTypeException("EPIC tasks cannot be moved");
            case STORY -> {
                if (newParent != null && newParent.getTaskType() != EPIC) {
                    throw new TaskValidationException("STORY can only be moved under EPIC");
                }
            }
            case TASK -> {
                if (newParent == null || newParent.getTaskType() != STORY) {
                    throw new TaskValidationException("TASK must have STORY parent");
                }
            }
        }
    }

    private void preventInfiniteLoop(Long taskId, Long parentId){
        if(parentId == null) return;

        // Method 1: Check if parentId is descendant of taskId
        if (isDescendantOf(parentId, taskId)) {
            throw new TaskValidationException("Cannot move task under its own descendant");
        }

        // Method 2: Additional safety - traverse upward from new parent
        Task ancestor = taskRepository.findByIdAndDeletedAtIsNull(parentId).orElse(null);
        Set<Long> visited = new HashSet<>();
        int depth = 0;
        final int MAX_DEPTH = 10;
        while( ancestor != null && depth < MAX_DEPTH) {
            if(visited.contains(ancestor.getId())) {
                throw new TaskValidationException(("Circular reference detected in task hierarchy"));
            }
            if(ancestor.getId().equals(taskId)) {
                throw new TaskValidationException("Cannot move task under its own descendant");
            }
            visited.add(ancestor.getId());
            ancestor = ancestor.getParentTask();
            depth++;
        }
        if(depth >= MAX_DEPTH) {
            throw new TaskValidationException("Task hierarchy depth exceeds maximum allowed");
        }

    }

    private boolean isDescendantOf(Long potentialDescendant, Long ancestorId){
        Set<Long> visited = new HashSet<>();
        Queue<Long> toCheck = new LinkedList<>();
        toCheck.add(ancestorId);

        while (!toCheck.isEmpty()) {
            Long currentId = toCheck.poll();
            if (visited.contains(currentId)) continue;
            visited.add(currentId);

            List<Task> children = taskRepository.findAllChildrenByParentTaskId(currentId);
            for (Task child : children) {
                if (child.getId().equals(potentialDescendant)) {
                    return true;
                }
                toCheck.add(child.getId());
            }
        }
        return false;
    }

    private void recalculateProgress(Task task, Task currentParent, Task newParent){
        try {
            // Recalculation progress for old hierarchy
            if(currentParent != null) {
                switch (currentParent.getTaskType()) {
                    case STORY -> taskProgressService.propagateProgressToParent(currentParent.getId());
                    case EPIC -> taskProgressService.updateHierarchyProgress(currentParent.getId());
                }
            }
            // Recalculation progress for new hierarchy
            if(newParent != null) {
                switch (newParent.getTaskType()) {
                    case STORY -> taskProgressService.propagateProgressToParent(newParent.getId());
                    case EPIC -> taskProgressService.updateHierarchyProgress(newParent.getId());
                }
            }
        } catch (Exception e){
            log.error("Error recalculating progress for task {}: {}", task.getId(), e.getMessage());
            // Don't fail the entire operation for progress calculation errors, but log them for monitoring
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
        log.debug("Loading subtasks for {} tasks: {}", Optional.of(allTaskIds.size()), allTaskIds);
    }
}
