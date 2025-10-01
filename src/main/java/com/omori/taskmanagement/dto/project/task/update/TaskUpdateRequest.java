package com.omori.taskmanagement.dto.project.task.update;

import com.omori.taskmanagement.model.project.Task;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
public class TaskUpdateRequest {
    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must be less than 255 characters")
    private String title;
    
    @Size(max = 1000, message = "Description must be less than 1000 characters")
    private String description;
    
    private LocalDateTime dueDate;
    private LocalDateTime startDate;
    
    @NotNull(message = "Priority is required")
    private Task.TaskPriority priority;
    
    @NotNull(message = "Status is required")
    private Task.TaskStatus status;
    
    @DecimalMin(value = "0.0", message = "Estimated hours must be positive")
    private Double estimatedHours;
    
    @DecimalMin(value = "0.0", message = "Actual hours must be positive")
    private Double actualHours;
    
    @Min(value = 0, message = "Progress must be between 0 and 100")
    @Max(value = 100, message = "Progress must be between 0 and 100")
    private Integer progress;

    private Long categoryId;
    private Long assignedToId;
    private Long workspaceId;
    private Integer sortOrder;
    private Boolean isRecurring;
    private Map<String, Object> recurrencePattern;
    private Map<String, Object> metadata;
}