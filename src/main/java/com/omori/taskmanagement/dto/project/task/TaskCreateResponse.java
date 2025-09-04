package com.omori.taskmanagement.dto.project.task;

import com.omori.taskmanagement.model.project.Task;

import java.time.LocalDateTime;

public record TaskCreateResponse(
        Long id,
        String title,
        String description,
        Task.TaskType type,
        Task.TaskPriority priority,
        LocalDateTime dueDate,
        LocalDateTime startDate
) {
    public static TaskCreateResponse from(Task task) {
        return new TaskCreateResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getTaskType(),
                task.getPriority(),
                task.getDueDate(),
                task.getStartDate()
        );
    }
}