package com.omori.taskmanagement.springboot.service;

import com.omori.taskmanagement.springboot.dto.project.CreateTaskRequest;
import com.omori.taskmanagement.springboot.exceptions.UserNotFoundException;
import com.omori.taskmanagement.springboot.model.project.Task;
import com.omori.taskmanagement.springboot.model.usermgmt.User;
import com.omori.taskmanagement.springboot.repository.project.TaskRepository;
import com.omori.taskmanagement.springboot.repository.usermgmt.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskServiceIml implements TaskService{

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;

    @Override
    public Task createTask(Long id,CreateTaskRequest request) {
        log.info("Creating task for user with id: {}", id);
        User user = userRepository
                .findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " +id));

        // if user id is found, assign it to the FK in task
        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .dueDate(request.getDueDate())
                .priority(request.getTaskPriority())
                .startDate(request.getStartDate())
                .estimatedHours(request.getEstimatedHours())
                .category(request.getCategory())
                .assignedTo(request.getAssignedTo())
                .workspace(request.getWorkspace())
                .progress(0)
                .status(Task.TaskStatus.pending)
                .user(user)
                .build();

        return taskRepository.save(task);
    }
}