package com.omori.taskmanagement.service.task.bulkOperation;

import com.omori.taskmanagement.dto.project.task.TaskResponse;
import com.omori.taskmanagement.model.project.Task;

import java.util.List;

public class TaskBulkOperationServiceImpl implements TaskBulkOperationService{
    @Override
    public List<TaskResponse> updateMultipleTasksStatus(List<Long> taskIds, Long userId, Task.TaskStatus status) {
        return List.of();
    }

    @Override
    public List<TaskResponse> updateMultipleTasksProgress(List<Long> taskIds, Long userId, Integer progress) {
        return List.of();
    }

    @Override
    public List<TaskResponse> assignMultipleTasks(List<Long> taskIds, Long userId, Long assigneeId) {
        return List.of();
    }

    @Override
    public List<TaskResponse> updateMultipleTasksPriority(List<Long> taskIds, Long userId, Task.TaskPriority priority) {
        return List.of();
    }

    @Override
    public void deleteMultipleTasks(List<Long> taskIds, Long userId) {

    }

    @Override
    public void archiveMultipleTasks(List<Long> taskIds, Long userId) {

    }

    @Override
    public void moveMultipleTasksToParent(List<Long> taskIds, Long newParentId, Long userId) {

    }

    @Override
    public List<TaskResponse> convertMultipleTasksType(List<Long> taskIds, Task.TaskType targetType, Long userId) {
        return List.of();
    }
}
