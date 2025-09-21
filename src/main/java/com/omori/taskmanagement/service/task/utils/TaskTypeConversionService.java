package com.omori.taskmanagement.service.task.utils;

import com.omori.taskmanagement.model.project.Task;

public interface TaskTypeConversionService {

    Task convertStoryToEpic(Long storyId, Long userId);
    Task convertEpicToStory(Long epicId, Long newParentId, Long userId);
    boolean canConvertType(Long taskId, Task.TaskType targetType);
}
