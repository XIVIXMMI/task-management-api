package com.omori.taskmanagement.dto.project.task;

import com.omori.taskmanagement.model.project.Task;
import lombok.Data;

import java.time.Instant;

@Data
public class TaskFilterRequest {
    private Task.TaskStatus status;
    private Task.TaskPriority priority;
    private Long categoryId;
    private Long workspaceId;
    private Instant dueDateFrom;
    private Instant dueDateTo;
    private String keyword;
    private Boolean isOverdue;
    private int page = 0;
    private int size = 20;
    private String sortBy = "createdAt";
    private String sortDirection = "desc";
}