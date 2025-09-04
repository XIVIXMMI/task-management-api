package com.omori.taskmanagement.dto.project.task;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.omori.taskmanagement.dto.project.subtask.SubtaskResponse;
import com.omori.taskmanagement.model.project.Task;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;

@JsonIgnoreProperties(ignoreUnknown = true) // ignore unknown properties during deserialization
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class TaskResponse implements Serializable {
    private static final long serialVersionUID = 1L;

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

    @Builder.Default
    private List<SubtaskResponse> subtasks = new ArrayList<>();
    
    private Task.TaskType taskType;
    private Long parentTaskId;
    private String parentTaskTitle;

    public static TaskResponse from(Task task) {
        return TaskResponse.builder()
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
                .taskType(task.getTaskType())
                .parentTaskId(task.getParentTask() != null ? task.getParentTask().getId() : null)
                .parentTaskTitle(task.getParentTask() != null ? task.getParentTask().getTitle() : null)
                .build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        TaskResponse that = (TaskResponse) o;
        return Objects.equals(id, that.id) && Objects.equals(uuid, that.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, uuid);
    }

    @Override
    public String toString() {
        return "TaskResponse{" +
                "id=" + id +
                ", uuid=" + uuid +
                ", title='" + title + '\'' +
                ", status=" + status +
                ", priority=" + priority +
                '}';
    }
}