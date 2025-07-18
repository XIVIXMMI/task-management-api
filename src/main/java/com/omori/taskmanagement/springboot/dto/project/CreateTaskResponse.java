package com.omori.taskmanagement.springboot.dto.project;

import com.omori.taskmanagement.springboot.model.project.Task;

import java.time.LocalDateTime;

public record CreateTaskResponse(
        Long id,
        String title,
        String description,
        Task.TaskPriority priority,
        LocalDateTime dueDate,
        LocalDateTime startDate
) {
    public static CreateTaskResponse from(Task task) {
        return new CreateTaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getPriority(),
                task.getDueDate(),
                task.getStartDate()
        );
    }
}