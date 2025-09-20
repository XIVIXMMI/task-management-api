package com.omori.taskmanagement.service.task;

import com.omori.taskmanagement.exceptions.task.TaskAccessDeniedException;
import com.omori.taskmanagement.model.project.Task;
import com.omori.taskmanagement.repository.project.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskAccessControlServiceImpl implements TaskAccessControlService{

    private final TaskRepository taskRepository;

    @Override
    public void validateTaskAccess(Task task, Long userId) {
        if(task == null){
            return;
        }
        if( userId == null ){
            throw new IllegalArgumentException("User ID must be provided to validate access");
        }
        if((task.getUser() == null || !task.getUser().getId().equals(userId)) &&
                (task.getAssignedTo() == null || !task.getAssignedTo().getId().equals(userId))) {
            throw new TaskAccessDeniedException("Access denied user" + userId + " with task id: " + task);
        }
    }
}
