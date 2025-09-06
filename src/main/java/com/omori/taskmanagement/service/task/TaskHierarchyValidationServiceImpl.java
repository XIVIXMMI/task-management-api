package com.omori.taskmanagement.service.task;

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
public class TaskHierarchyValidationServiceImpl implements TaskHierarchyValidation{

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

//        Step 2: Validate Epic Task
//         1. Find the Epic task in allTasks
//         2. Ensure it's actually EPIC type
//         3. Use validateTaskType(epic) to check it has no parent

//        Step 3: Run All Validations
//         1. Validate each task type: for each task -> validateTaskType(task)
//         2. Validate sort orders: validateSortOrder(allTasks)
//         3. Additional hierarchy checks (circular references, orphans, etc.)

//        Step 4: Collect All Violations
//        List<String> violations = new ArrayList<>();
//        try {
//            // Run each validation
//        } catch (TaskValidationException e) {
//            violations.add(e.getMessage());
//        }
//        // Continue with other validations...

//        Step 5: Final Exception with Complete Report
//        if (!violations.isEmpty()) {
//            String message = "Hierarchy validation failed: " + String.join("; ", violations);
//            throw new TaskValidationException(message, details);
//        }
    }

    @Override
    public void validateTaskType(Task task) {
        if(task == null){
            throw new IllegalArgumentException("Task cannot be null");
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
            case EPIC:
                if( parentTask != null ) {
                    throw new TaskValidationException("EPIC task cannot have a parent task",
                            createValidationDetails(task,parentTask,null));
                }
                break;
            case STORY:
                if( parentTask != null && parentTask.getTaskType() != Task.TaskType.EPIC){
                    throw new TaskValidationException(
                            "STORY task must have a parent task of type EPIC",
                            createValidationDetails(task ,parentTask,"EPIC"));
                }
                break;
            case TASK:
                if( parentTask != null && parentTask.getTaskType() != Task.TaskType.STORY){
                    throw new TaskValidationException(
                            "TASK task must have a parent task of type STORY",
                            createValidationDetails(task,parentTask,"STORY"));
                }
                break;
                default:
                    throw new InvalidTaskTypeException("Unknow task type: " + task.getTaskType());
        }
    }

    @Override
    public void validateSortOrder(List<Task> tasks) {
        // Group tasks: same parent = same validation group
        Map<Long, List<Task>> taskGroups = tasks.stream()
                .collect(Collectors.groupingBy(
                        task -> task.getParentTask() != null ? task.getParentTask().getId() : -1L));
        // Don't fail on the first error - collect ALL issues
        List<String> violations = new ArrayList<>();
        // For each parent group, check:
        for(Map.Entry< Long, List<Task>> entry : taskGroups.entrySet()){
            List<Task> tasksInGroup = entry.getValue();
            // - No huge gaps in sequence
            List<Integer> sortOrders = tasksInGroup.stream()
                    .map(Task::getSortOrder)
                    .sorted()
                    .toList();
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
            String message = "Validation failed for task hierarchy: " + String.join(", ", violations);
            throw new TaskValidationException(message);
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
