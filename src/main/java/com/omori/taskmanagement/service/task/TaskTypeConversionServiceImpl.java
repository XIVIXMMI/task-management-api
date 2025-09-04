package com.omori.taskmanagement.service.task;

import com.omori.taskmanagement.model.project.Task;

public class TaskTypeConversionServiceImpl implements TaskTypeConversionService{
    @Override
    public Task convertStoryToEpic(Long storyId, Long userId) {
        return null;
    }

    @Override
    public Task convertEpicToStory(Long epicId, Long newParentId, Long userId) {
        return null;
    }

    @Override
    public boolean canConvertType(Long taskId, Task.TaskType targetType) {
        return false;
    }
}
