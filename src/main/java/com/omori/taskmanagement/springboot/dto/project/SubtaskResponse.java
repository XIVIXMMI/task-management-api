package com.omori.taskmanagement.springboot.dto.project;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.omori.taskmanagement.springboot.model.project.Subtask;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class SubtaskResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String title;
    private String description;
    private Long taskId;
    private String taskTitle;
    private Integer sortOrder;
    private Boolean isCompleted;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static SubtaskResponse from(Subtask subtask) {
        return SubtaskResponse.builder()
                .id(subtask.getId())
                .title(subtask.getTitle())
                .description(subtask.getDescription())
                .taskId(subtask.getTask() != null ? subtask.getTask().getId() : null)
                .taskTitle(subtask.getTask() != null ? subtask.getTask().getTitle() : null)
                .sortOrder(subtask.getSortOrder())
                .isCompleted(subtask.getIsCompleted())
                .completedAt(subtask.getCompletedAt())
                .createdAt(subtask.getCreatedAt())
                .updatedAt(subtask.getUpdatedAt())
                .build();
    }

    @Override
    public String toString() {
        return "SubtaskResponse{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", isCompleted=" + isCompleted +
                ", taskId=" + taskId +
                ", sortOrder=" + sortOrder +
                '}';
    }
}