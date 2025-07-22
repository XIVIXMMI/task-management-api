package com.omori.taskmanagement.springboot.dto.project;

import com.omori.taskmanagement.springboot.model.project.Task;
import lombok.Data;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
public class GetTaskResponse {
    private Long id;
    private UUID uuid;
    private String title;
    private String description;
    private LocalDateTime dueDate;
    private LocalDateTime startDate;
    private LocalDateTime completedAt;
    private Task.TaskPriority priority;
    private Task.TaskStatus status;
    private Double estimatedHours;
    private Double actualHours;
    private Integer progress;
    private String categoryName;
    private String assignedToName;
    private String workspaceName;
    private Integer sortOrder;
    private Boolean isRecurring;
    private Map<String, Object> recurrencePattern;
    private Map<String, Object> metadata;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public static GetTaskResponse from(Task task) {
        return GetTaskResponse.builder()
                .id(task.getId())
                .uuid(task.getUuid())
                .title(task.getTitle())
                .description(task.getDescription())
                .dueDate(task.getDueDate())
                .startDate(task.getStartDate())
                .completedAt(task.getCompletedAt())
                .priority(task.getPriority())
                .status(task.getStatus())
                .estimatedHours(task.getEstimatedHours())
                .actualHours(task.getActualHours())
                .progress(task.getProgress())
                .categoryName(task.getCategory() != null ? task.getCategory().getName() : null)
                .assignedToName(task.getAssignedTo() != null ? task.getAssignedTo().getUsername() : null)
                .workspaceName(task.getWorkspace() != null ? task.getWorkspace().getName() : null)
                .sortOrder(task.getSortOrder())
                .isRecurring(task.getIsRecurring())
                .recurrencePattern(task.getRecurrencePattern())
                .metadata(task.getMetadata())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }
}