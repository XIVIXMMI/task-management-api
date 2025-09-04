package com.omori.taskmanagement.service.task;

import com.omori.taskmanagement.dto.project.subtask.SubtaskResponse;
import com.omori.taskmanagement.dto.project.task.HierarchyEpicDto;
import com.omori.taskmanagement.dto.project.task.StoryWithTaskDto;
import com.omori.taskmanagement.dto.project.task.TaskResponse;
import com.omori.taskmanagement.exceptions.task.TaskNotFoundException;
import com.omori.taskmanagement.exceptions.task.TaskValidationException;
import com.omori.taskmanagement.model.project.Subtask;
import com.omori.taskmanagement.model.project.Task;
import com.omori.taskmanagement.repository.project.SubtaskRepository;
import com.omori.taskmanagement.repository.project.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor

public class TaskHierarchyServiceImpl implements TaskHierarchyService{

    private final TaskRepository taskRepository;
    private final SubtaskRepository subtaskRepository;

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

        if (epic.getTaskType() != Task.TaskType.EPIC) {
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
        if (!hierarchy.getStories().isEmpty()) {
            loadSubtasksForHierarchy(hierarchy);
        }
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

        if(epic.getTaskType() != Task.TaskType.EPIC){
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
        if (!hierarchy.getStories().isEmpty()) {
            loadSubtasksForHierarchy(hierarchy);
        }
        return hierarchy;
    }

    @Override
    public List<Task> getEpicChildren(Long parentId) {
        return List.of();
    }

    @Override
    public void moveTaskToParent(Long taskId, Long parentId) {

    }

    @Override
    public void validateHierarchy(Long epicId) {

    }

    @Override
    public List<Task> getChildTasks(Long parentTaskId) {
        log.debug("Getting children for epic task with ID {} ", parentTaskId);
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
        return List.of();
    }

    @Override
    public Task getParentTask(Long taskId) {
        return null;
    }

    @Override
    public int getHierarchyDepth(Long taskId) {
        return 0;
    }

    @Override
    public Integer getNextSortOrderForParent(Long parentTaskId) {
        // Implementation to get next sort order
        return taskRepository.findMaxSortOrderByParentTaskId(parentTaskId)
                .map(max -> max + 1)
                .orElse(0);
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
}
