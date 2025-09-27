package com.omori.taskmanagement.service.task.hierarchy;

import com.omori.taskmanagement.exceptions.task.InvalidTaskTypeException;
import com.omori.taskmanagement.exceptions.task.TaskValidationException;
import com.omori.taskmanagement.model.project.Task;
import com.omori.taskmanagement.repository.project.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class TaskHierarchyValidationServiceImpl implements TaskHierarchyValidationService {

    private final TaskRepository taskRepository;

    @Override
    public void validateHierarchy(Long epicId) {
        log.debug("Validating hierarchy for epic task with ID {} ", epicId);
        if(epicId == null){
            throw new TaskValidationException("Epic ID cannot be null");
        }
        Task epicTask = taskRepository.findById(epicId)
                .orElseThrow(() -> new TaskValidationException("Epic task not found with ID: " + epicId));
        if(epicTask.getTaskType() != Task.TaskType.EPIC){
            throw new TaskValidationException("Task with id " + epicId + " is not an EPIC task",
                    Map.of("taskType", "Expected EPIC but found " + epicTask.getTaskType()));
        }
        List<Task> allTasks = taskRepository.findAllTasksUnderEpic(epicId);
        List<String> violations = new ArrayList<>();
        // EpicTask-level checks -> must not have a parent-task
        try {
            validateTaskType(epicTask);
        } catch (TaskValidationException t) {
            violations.add("EPIC: " + epicId + ": "+ t.getMessage());
        }
        // Check single tasks
        for(Task task : allTasks) {
            try {
                validateTaskType(task);
            } catch (TaskValidationException t) {
                violations.add(task.getId() + ": " + t.getMessage());
            }
        }
        // SortOder checks
        try{
            // Filter out a top-level task before sort validation
            List<Task> taskWithParent = allTasks.stream()
                    .filter(t -> t.getParentTask() != null)
                    .toList();
            if(!taskWithParent.isEmpty()){
                validateSortOrder(taskWithParent);
            }
        } catch (TaskValidationException t) {
            violations.add("Sort order validation failed: " + t.getMessage());
        }
        if(!violations.isEmpty()){
            String message = "Hierarchy validation failed: " + violations.size() + " issues found";
            Map<String, String> details = Map.of(
                    "epicId", epicId.toString(),
                    "violationCount", String.valueOf(violations.size()),
                    "violations", String.join("; ", violations)
            );
            throw new TaskValidationException(message, details);
        }
        log.debug("Validation of hierarchy for epic task with ID {} succeeded", epicId);
    }

    @Override
    public void validateTaskType(Task task) {
        if(task == null){
            throw new IllegalArgumentException("Task Hierarchy cannot be null");
        }
        if(task.getTaskType() == null) {
            throw new IllegalArgumentException("Task type cannot be null");
        }
        Task parentTask = task.getParentTask();
        if(parentTask == null) {
            log.debug("Task {} is standalone (no parent)", task.getId());
        }else {
            log.debug("Validating task {} (type: {}) with parent {} (type: {})",
                    task.getId(), task.getTaskType(),
                    parentTask.getId(), parentTask.getTaskType());
        }
        switch (task.getTaskType()) {
            case EPIC -> {
                if (parentTask != null) {
                    throw new TaskValidationException("EPIC task cannot have a parent task",
                            createValidationDetails(task, parentTask, null));
                }
            }
            case STORY -> {
                if (parentTask != null && parentTask.getTaskType() != Task.TaskType.EPIC) {
                    throw new TaskValidationException(
                            "STORY task must have a parent task of type EPIC",
                            createValidationDetails(task, parentTask, "EPIC"));
                }
            }
            case TASK -> {
                if (parentTask != null && parentTask.getTaskType() != Task.TaskType.STORY) {
                    throw new TaskValidationException(
                            "TASK task must have a parent task of type STORY",
                            createValidationDetails(task, parentTask, "STORY"));
                }
            }
            default -> throw new InvalidTaskTypeException("Unknow task type: " + task.getTaskType());
        }
    }

    @Override
    public void validateSortOrder(List<Task> tasks) {
        if(tasks == null || tasks.isEmpty()){
            return;
        }
        // Group tasks: same parent = same validation group
        Map<Long, List<Task>> taskGroups = tasks.stream()
                .collect(Collectors.groupingBy(
                        task -> Long.valueOf(task.getParentTask() != null ? task.getParentTask().getId() : -1L)));
        // Don't fail on the first error - collect ALL issues
        List<String> violations = new ArrayList<>();
        // For each parent group, check:
        for(Map.Entry< Long, List<Task>> entry : taskGroups.entrySet()){
            // Skip root-level (no parent) group; root items typically aren't ordered against each other here
            if (entry.getKey() == -1L) {
                continue;
            }
            List<Task> tasksInGroup = entry.getValue();
            // - No huge gaps in sequence
            List<Integer> sortOrdersRaw = tasksInGroup.stream()
                    .map(Task::getSortOrder)
                    .toList();
            // - No null sort orders
            if (sortOrdersRaw.stream().anyMatch(Objects::isNull)) {
                violations.add("Null sort order found for parent task " + entry.getKey());
                continue; // avoid NPEs in the checks below
            }
            List<Integer> sortOrders = sortOrdersRaw.stream().sorted().toList();
            // - No duplicate sort orders
            Set<Integer> orders = new HashSet<>(sortOrders);
            if( orders.size() < tasksInGroup.size() ){
                violations.add("Duplicate sort order found for parent task " + entry.getKey());
            }
            for( int i = 0; i <sortOrders.size(); i++){
                int curr = sortOrders.get(i);
                // Check negative
                if( curr < 0 ){
                    violations.add("Negative sort order for parent task " + entry.getKey());
                }
                // Check gaps (skip for the first element)
                if( i > 0 && curr - sortOrders.get(i-1) > 1 ){
                    violations.add("Gap in sort order sequence for parent task " + entry.getKey());
                }
            }
        }
        if(!violations.isEmpty()){
            List<Long> allTaskIds = tasks.stream()
                    .map(Task::getId)
                    .toList();
            String message = "Sort order validation failed: " + violations.size() + " issues found";
            Map<String, String> details = Map.of(
                    "affectedTaskIds", allTaskIds.stream()
                            .map(String::valueOf).
                            collect(Collectors.joining(", ")) ,
                    "violationCount", String.valueOf(violations.size()),
                    "violations", String.join("; ", violations)
            );
            throw new TaskValidationException(message, details);
        }
    }

    private Map<String, String> createValidationDetails(Task task, Task parentTask, String expectedParentType) {
        Map<String, String> details = new HashMap<>();
        details.put("taskId", task.getId().toString());
        details.put("taskType", task.getTaskType().toString());
        if(parentTask != null) {
            details.put("parentId", parentTask.getId().toString());
            details.put("parentType", parentTask.getTaskType().toString());
        }
        if(expectedParentType != null) {
            details.put("expectedParentType", expectedParentType);
        }
        return details;
    }
}
