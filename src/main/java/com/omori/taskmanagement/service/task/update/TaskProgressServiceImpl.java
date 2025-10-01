package com.omori.taskmanagement.service.task.update;

import com.omori.taskmanagement.exceptions.task.TaskBusinessException;
import com.omori.taskmanagement.exceptions.task.TaskNotFoundException;
import com.omori.taskmanagement.exceptions.task.TaskValidationException;
import com.omori.taskmanagement.model.events.TaskProgressUpdateEvent;
import com.omori.taskmanagement.model.project.Subtask;
import com.omori.taskmanagement.model.project.Task;
import com.omori.taskmanagement.repository.project.TaskRepository;
import com.omori.taskmanagement.service.subtask.SubTaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class TaskProgressServiceImpl implements TaskProgressService{

    private final TaskRepository taskRepository;
    private final SubTaskService subTaskService;

    private final Task.TaskType EPIC = Task.TaskType.EPIC;
    private final Task.TaskType STORY = Task.TaskType.STORY;


    @Override
    public int calculateTaskProgress(Long taskId) {
        // Calculate the percentage of completed subtasks for this task
        List<Subtask> subtasks = subTaskService.getSubtasksByTaskId(taskId);
        if(subtasks.isEmpty()){
            return 0;
        }

        long completed = subtasks.stream()
                .mapToLong( s -> Boolean.TRUE.equals(s.getIsCompleted()) ? 1 : 0)
                .sum();
        return (int) ((completed * 100) / subtasks.size());
    }

    @Override
    @Transactional
    public void updateProgressFromSubtasks(Long taskId) {
        log.debug("Updating progress from subtask for task with ID {} ", taskId);

        Task task = taskRepository.findByIdWithRelations(taskId)
                .orElseThrow(() -> new TaskBusinessException("Task not found with ID: " + taskId));

        List<Subtask> subtasks = subTaskService.getSubtasksByTaskId(taskId);
        if (subtasks.isEmpty()){
            log.debug("No subtasks found for task with ID {} ", taskId);
            return ;
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

            if (parentType == STORY) {
                propagateProgressToParent(parentId);
            } else if (parentType == EPIC) {
                updateHierarchyProgress(parentId);
            }

            log.debug("Successfully triggered update to parent task with ID {} of type {}", taskId, parentType);
        }
    }

    @Override
    @Transactional
    public void propagateProgressToParent(Long storyTaskId){
        Task story = taskRepository.findByIdWithRelations(storyTaskId)
                .orElseThrow(() -> new TaskBusinessException("Task not found with ID: " + storyTaskId));

        if( story.getTaskType() != STORY){
            throw new TaskValidationException("Task with id + " + storyTaskId + " is not a STORY Task",
                    Map.of("taskType","Excepted STORY but found " + story.getTaskType()));
        }

        // Get own subtasks progress: 0-100%
        int avgSubtaskProgress = calculateTaskProgress(storyTaskId);

        // Get child tasks average progress: 0-100%
        List<Task> tasks = taskRepository.findByParentTaskIdAndTaskTypeAndDeletedAtIsNull(storyTaskId, Task.TaskType.TASK);
        log.debug("Found {} child tasks for story ID {}: {}",
                tasks.size(), storyTaskId,
                tasks.stream().map(Task::getId).collect(Collectors.toList()));

        int finalProgress;
        if (tasks.isEmpty()) {
            finalProgress = avgSubtaskProgress;  // Only own progress when no children
        } else {
            double avgChildProgress = calculateChildrenAverageProgress(tasks);
            finalProgress = calculateWeightedProgress(avgSubtaskProgress, avgChildProgress);
        }

        story.setProgress(finalProgress);
        story.setUpdatedAt(LocalDateTime.now());
        taskRepository.save(story);
        if(story.getParentTask() != null && story.getParentTask().getId() != null ) {
            updateHierarchyProgress(story.getParentTask().getId());
        }
        log.debug("Updated progress for story task with ID {} ", storyTaskId);
    }

    @Override
    @Transactional
    public void updateHierarchyProgress(Long epicTaskId) {
        Task epic = taskRepository.findById(epicTaskId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with ID: " + epicTaskId));
        if( epic.getTaskType() != Task.TaskType.EPIC){
            throw new TaskValidationException("Task with id + " + epicTaskId + " is not an EPIC Task",
                    Map.of("taskType","Excepted EPIC but found " + epic.getTaskType()));
        }
        int ownProgress = calculateTaskProgress(epicTaskId);

        List<Task> stories = taskRepository.findByParentTaskIdAndTaskTypeAndDeletedAtIsNull(epicTaskId, Task.TaskType.STORY);

        int finalProgress;
        if(stories.isEmpty()){
            finalProgress = ownProgress;
        } else {
            double childrenProgress = calculateChildrenAverageProgress(stories);
            finalProgress = calculateWeightedProgress(ownProgress, childrenProgress);
        }

        epic.setProgress(finalProgress);
        epic.setUpdatedAt(LocalDateTime.now());
        taskRepository.save(epic);
        log.debug("Updated progress for epic task with ID {} ", epicTaskId);
    }

    /**
     * =========UTILITIES========
     **/

    private double calculateChildrenAverageProgress(List<Task> children) {
        // Calculate the average progress of child tasks
        if (children.isEmpty()) {
            log.debug("No children found for parent task ");
            return 0.0;
        }

        return children.stream()
                .mapToInt( t -> Optional.ofNullable(t.getProgress()).orElse(0) )
                .average()
                .orElse(0.0);
    }

    private int calculateWeightedProgress(int ownProgress, double childrenProgress) {
        // Apply 50/50 weighted formula
        return (int) ((ownProgress * 0.5) + (childrenProgress * 0.5));
    }

    /**
     * Event listener that handles task progress updates triggered by subtask changes.
     * This breaks the circular dependency between SubTaskService and TaskHybridService.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTaskProgressUpdateEvent(TaskProgressUpdateEvent event) {
        log.debug("Handling task progress update event for task ID: {} - Reason: {}",
                event.getTaskId(), event.getReason());
        updateProgressFromSubtasks(event.getTaskId());
    }

}
