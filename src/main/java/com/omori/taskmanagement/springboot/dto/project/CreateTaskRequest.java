package com.omori.taskmanagement.springboot.dto.project;

import com.omori.taskmanagement.springboot.model.project.Category;
import com.omori.taskmanagement.springboot.model.project.Task;
import com.omori.taskmanagement.springboot.model.project.Workspace;
import com.omori.taskmanagement.springboot.model.usermgmt.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class CreateTaskRequest {

    @NotBlank( message = "Title is not blank")
    @Schema(description = "Title of task", example = "self study")
    private String title;

    @Schema(description = "Description of task", example = "Trying to learn Rust")
    private String description;

    @NotNull(message = "Due date is not null")
    @Schema(description = "Date of task should be completed, format should be YYYY-MM-DD HH:MM:SS (ISO-8601)", example = "2025-08-05T23:59:00")
    private LocalDateTime dueDate;

    @NotNull( message = "Start date is not null")
    @Schema(description = "Date start of task, format should be YYYY-MM-DD HH:MM:SS (ISO-8601)", example = "2025-07-05T23:59:00")
    private LocalDateTime startDate;

    private Task.TaskStatus status;

    @NotNull( message = "Task priority is not null")
    @Schema(description = "Important level of task (low,medium,high,urgent)", example = "medium")
    private Task.TaskPriority taskPriority;

    @Schema(description = "Estimated completion time", example = "3.5")
    private Double estimatedHours;

    @Schema(description = "Category of task", example = "projects, team")
    private Category category;

    @Schema(description = "ID of user assigned the task", example = "10")
    private User assignedTo;

    @Schema(description = "ID of workspace", example = "1")
    private Workspace workspace;

    private Integer progress;

    private Integer sortOrder;

    private Boolean isRecurring;

    private Map<String, Object> recurrencePattern;

    private Map<String, Object> metadata;

}
