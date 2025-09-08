package com.omori.taskmanagement.service.task;

import com.omori.taskmanagement.model.project.Task;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskTypeConversionServiceImpl implements TaskTypeConversionService {

    @Override
    @Transactional
    public Task convertStoryToEpic(Long storyId, Long userId) {
        throw new UnsupportedOperationException("convertStoryToEpic not implemented yet");
    }

    @Override
    @Transactional
    public Task convertEpicToStory(Long epicId, Long newParentId, Long userId) {
        throw new UnsupportedOperationException("convertEpicToStory not implemented yet");
    }

    @Override
    public boolean canConvertType(Long taskId, Task.TaskType targetType) {
        throw new UnsupportedOperationException("canConvertType not implemented yet");
    }
}
