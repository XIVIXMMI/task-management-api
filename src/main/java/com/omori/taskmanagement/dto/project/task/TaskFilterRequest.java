package com.omori.taskmanagement.dto.project.task;

import com.omori.taskmanagement.model.project.Task;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TaskFilterRequest {

    private Task.TaskStatus status;
    private Task.TaskPriority priority;
    private Long categoryId;
    private Long workspaceId;
    private Instant dueDateFrom;
    private Instant dueDateTo;
    private String keyword;
    private Boolean isOverdue;
    @Builder.Default private int page = 0;
    @Builder.Default private int size = 20;
    @Builder.Default private String sortBy = "createdAt";
    @Builder.Default private String sortDirection = "desc";

    public TaskFilterRequest createDefaultFilter() {
        TaskFilterRequest filter = new TaskFilterRequest();
        filter.page = 0;
        filter.size = 20;
        filter.sortBy = "createdAt";
        filter.sortDirection = "desc";
        return filter;
    }
}