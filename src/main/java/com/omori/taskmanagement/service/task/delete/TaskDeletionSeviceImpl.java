package com.omori.taskmanagement.service.task.delete;

import java.util.List;

public class TaskDeletionSeviceImpl implements TaskDeletionService{
    @Override
    public void softDeleteTaskWithChildren(Long taskId, Long userId) {

    }

    @Override
    public void softDeleteMultipleTasksWithChildren(List<Long> taskIds, Long userId) {

    }

    @Override
    public void archiveTaskWithChildren(Long taskId, Long userId) {

    }

    @Override
    public void archiveMultipleTasksWithChildren(List<Long> taskIds, Long userId) {

    }

    @Override
    public void restoreTaskWithChildren(Long taskId, Long userId) {

    }

    @Override
    public void permanentlyDeleteTask(Long taskId, Long userId) {

    }

    @Override
    public List<Long> getAllChildTaskIds(Long parentTaskId) {
        return List.of();
    }

    @Override
    public boolean canDeleteTask(Long taskId, Long userId) {
        return false;
    }
}
