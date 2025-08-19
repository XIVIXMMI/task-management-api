package com.omori.taskmanagement.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;

import com.omori.taskmanagement.dto.project.SubtaskCreateRequest;
import com.omori.taskmanagement.dto.project.SubtaskRequest;
import com.omori.taskmanagement.dto.project.SubtaskUpdateRequest;
import com.omori.taskmanagement.exceptions.task.SubtaskNotFoundException;
import com.omori.taskmanagement.exceptions.task.TaskNotFoundException;
import com.omori.taskmanagement.model.project.Subtask;
import com.omori.taskmanagement.model.project.Task;
import com.omori.taskmanagement.repository.project.SubtaskRepository;
import com.omori.taskmanagement.repository.project.TaskRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class SubTaskServiceImpl implements SubTaskService {

    private final SubtaskRepository subTaskRepository;
    private final TaskRepository taskRepository;

    // may combine 2 createSubtask function
    @Override
    public void createSubtask(SubtaskCreateRequest subtask, Long taskId, String title) {
        log.info("Creating subtask with title: {} for task ID: {}", title, taskId);
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> {
                    log.warn("Task not found with ID: {}", taskId);
                    return new TaskNotFoundException("Task not found with id: " + taskId);
                });
        Integer nextSortOrder = getNextSortOrder(taskId);

        Subtask newSubtask = Subtask.builder()
                .title(title)
                .description(subtask != null && subtask.getDescription() != null ? subtask.getDescription() : "")
                .sortOrder(nextSortOrder)
                .task(task)
                .isCompleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        subTaskRepository.save(newSubtask);
    }

    @Override
    public Subtask createSubtask(SubtaskRequest request) {
        log.info("Creating subtask with title: {} for task ID: {}", request.getTitle(), request.getTaskId());
        Task task = taskRepository.findById(request.getTaskId())
                .orElseThrow(() -> {
                    log.warn("Task not found with ID: {}", request.getTaskId());
                    return new TaskNotFoundException("Task not found with id: " + request.getTaskId());
                });
        Integer sortOrder = request.getSortOrder() != null ? request.getSortOrder()
                : getNextSortOrder(request.getTaskId());

        Subtask newSubtask = Subtask.builder()
                .task(task)
                .title(request.getTitle())
                .description(request.getDescription())
                .sortOrder(sortOrder)
                .isCompleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return subTaskRepository.save(newSubtask);
    }

    @Override
    public Subtask updateSubtask(Long subtaskId, SubtaskUpdateRequest request) {
        Subtask subtask = subTaskRepository.findByIdAndDeletedAtIsNull(subtaskId)
                .orElseThrow(() -> new SubtaskNotFoundException("Subtask not found with id: " + subtaskId));

        if (request.getTitle() != null) {
            subtask.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            subtask.setDescription(request.getDescription());
        }
        if (request.getSortOrder() != null) {
            subtask.setSortOrder(request.getSortOrder());
        }
        subtask.setUpdatedAt(LocalDateTime.now());

        return subTaskRepository.save(subtask);
    }

    @Override
    public Subtask toggleSubtaskCompletion(Long subtaskId) {
        Subtask subtask = subTaskRepository.findByIdAndDeletedAtIsNull(subtaskId)
                .orElseThrow(() -> new SubtaskNotFoundException("Subtask not found with id: " + subtaskId));

        subtask.setIsCompleted(!subtask.getIsCompleted());
        subtask.setUpdatedAt(LocalDateTime.now());

        if (subtask.getIsCompleted()) {
            subtask.setCompletedAt(LocalDateTime.now());
        } else {
            subtask.setCompletedAt(null);
        }
        log.info("Toggling completion status for subtask with ID: {} " , subtaskId);
        return subTaskRepository.save(subtask);
    }

    @Override
    public List<Subtask> getSubtasksByTaskId(Long taskId) {
        log.info("Retrieving subtasks for task with ID: {}", taskId);
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> {
                    log.warn("Task not found with ID: {}", taskId);
                    return new TaskNotFoundException("Task not found with id: " + taskId);
                });

        List<Subtask> subtasks = subTaskRepository.findByTaskIdAndDeletedAtIsNullOrderBySortOrder(task.getId());

        log.debug("Retrieved {} subtasks for task ID: {}", subtasks.size(), taskId);
        return subtasks;
    }

    @Override
    public List<Subtask> reorderSubtasks(Long taskId, List<Long> subtaskIds) {
        log.info("Reordering {} subtasks for task ID {}", subtaskIds.size(), taskId);
        List<Subtask> subtasks = new ArrayList<>();

        if (subtaskIds.isEmpty()) {
            log.warn("Attempted to reorder subtasks with empty subtask ID list for task {}", taskId);
            return Collections.emptyList();
        }

        for (int i = 0; i < subtaskIds.size(); i++) {
            Long subtaskId = subtaskIds.get(i);
            Subtask subtask = subTaskRepository.findById(subtaskId)
                    .orElseThrow(() -> new SubtaskNotFoundException("Subtask not found with id: " + subtaskId));
            if (!subtask.getTask().getId().equals(taskId)) {
                log.warn("Subtask ID {} does not belong to task ID {}", subtaskId, taskId);
                throw new TaskNotFoundException(
                        "Subtask with id " + subtaskId + " does not belong to task with id " + taskId);
            }
            subtask.setSortOrder(i);
            subtask.setUpdatedAt(LocalDateTime.now());
            subtasks.add(subtask);
        }
        return subTaskRepository.saveAll(subtasks);
    }

    @Override
    public Integer getNextSortOrder(Long taskId) {
        Long maxSortOrder = subTaskRepository.findMaxSortOrderByTaskId(taskId);
        /*
         * Casting from Long to int could cause overflow if maxSortOrder exceeds Integer.MAX_VALUE. 
         * Consider using Integer throughout or adding overflow protection.
         */
        Integer nextSortOrder = (maxSortOrder != null) ? (int) (maxSortOrder + 1) : 0;
        log.debug("Next sort order for task ID {} is {}", taskId, nextSortOrder);
        return nextSortOrder;
    }

    @Override
    public void deleteSubtask(Long subtaskId) {
        Subtask subtask = subTaskRepository.findById(subtaskId)
                .orElseThrow(() -> new SubtaskNotFoundException("Subtask not found with id: " + subtaskId));

        subTaskRepository.delete(subtask);
        log.info("Deleted subtask with ID: {}", subtaskId);
    }

    @Override
    public void softDeleteSubtask(Long subtaskId) {
        Subtask subtask = subTaskRepository.findByIdAndDeletedAtIsNull(subtaskId)
                .orElseThrow(() -> new SubtaskNotFoundException("Subtask not found with id: " + subtaskId));

        subtask.setDeletedAt(LocalDateTime.now());
        subTaskRepository.save(subtask);
        log.info("Soft deleted subtask with ID: {}", subtaskId);
    }

}
