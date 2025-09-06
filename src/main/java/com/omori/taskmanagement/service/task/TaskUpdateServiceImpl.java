package com.omori.taskmanagement.service.task;

import com.omori.taskmanagement.dto.project.task.TaskResponse;
import com.omori.taskmanagement.dto.project.task.TaskUpdateRequest;

@org.springframework.stereotype.Service
@lombok.extern.slf4j.Slf4j
@lombok.RequiredArgsConstructor
public class TaskUpdateServiceImpl implements TaskUpdateService{
    @Override
    @org.springframework.transaction.annotation.Transactional
    public TaskResponse updateTask(Long taskId, Long userId, TaskUpdateRequest request) {
        throw new UnsupportedOperationException("Unimplemented method 'updateTask'");
    }
}
