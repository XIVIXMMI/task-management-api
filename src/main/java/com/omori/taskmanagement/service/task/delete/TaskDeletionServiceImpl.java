package com.omori.taskmanagement.service.task.delete;

import java.util.List;

public class TaskDeletionServiceImpl implements TaskDeletionService{
    @Override
    public void softDeleteTask(Long taskId, Long userId) {

    }

    @Override
    public void softDeleteMultipleTasks(List<Long> taskIds, Long userId) {

    }

    @Override
    public void restoreTask(Long taskId, Long userId) {

    }

    @Override
    public void restoreMultipleTasks(List<Long> taskIds, Long userId) {

    }

    @Override
    public boolean canDeleteTask(Long taskId, Long userId) {
        return false;
    }
}
