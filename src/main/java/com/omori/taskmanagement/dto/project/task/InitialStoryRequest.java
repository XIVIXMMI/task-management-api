package com.omori.taskmanagement.dto.project.task;

import com.omori.taskmanagement.model.project.Task;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class InitialStoryRequest {

    @NotBlank(message = "Story Title is required")
    @Schema(description = "Title of the story (task level 1)", example = "Task 1.1")
    private String title;

    @Schema(description = "Description of the story", example = "Do the things 1.1")
    private String description;

    @Builder.Default
    @NotNull(message = "Start date is required")
    @Schema(description = "Date start of story, format should be YYYY-MM-DD HH:MM:SS (ISO-8601)",
            example = "2025-07-05T23:59:00",
            accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime startDate = LocalDateTime.now();

    @NotNull(message = "Due date is required for initial stories")
    @Future(message = "Due date must be in the future")
    @Schema(description = "Story due date", example = "2025-12-31T23:59:59")
    private LocalDateTime dueDate;

    @Builder.Default
    private Task.TaskPriority priority = Task.TaskPriority.medium;

    public TaskCreateRequest toTaskCreateRequest() {
        TaskCreateRequest taskCreateRequest = new TaskCreateRequest();
        taskCreateRequest.setTitle(title);
        taskCreateRequest.setDescription(description);
        taskCreateRequest.setStartDate(startDate == null ? LocalDateTime.now() : startDate);
        taskCreateRequest.setDueDate(dueDate);
        taskCreateRequest.setPriority(priority == null ? Task.TaskPriority.medium : priority);
        taskCreateRequest.setType(Task.TaskType.STORY);
        return taskCreateRequest;
    }
}
