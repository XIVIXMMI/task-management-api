package com.omori.taskmanagement.dto.project.task.creation;

import com.omori.taskmanagement.model.project.Task;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
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

    @Schema(description = "Date start of story, format should be YYYY-MM-DD HH:MM:SS (ISO-8601)",
            example = "2025-07-05T23:59:00",
            accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime startDate;

    @Future(message = "Due date must be in the future")
    @Schema(description = "Story due date", example = "2025-12-31T23:59:59")
    private LocalDateTime dueDate;

    @Builder.Default
    private Task.TaskPriority priority = Task.TaskPriority.medium;

    public BaseTaskCreateRequest toTaskCreateRequest() {
        BaseTaskCreateRequest baseTaskCreateRequest = new BaseTaskCreateRequest();
        baseTaskCreateRequest.setTitle(title);
        baseTaskCreateRequest.setDescription(description);
        LocalDateTime effectiveStartDate = startDate != null ? startDate : defaultStartDate();
        baseTaskCreateRequest.setStartDate(effectiveStartDate);
        baseTaskCreateRequest.setDueDate(dueDate != null ? dueDate : defaultDueDate(effectiveStartDate));
        baseTaskCreateRequest.setPriority(priority == null ? Task.TaskPriority.medium : priority);
        baseTaskCreateRequest.setType(Task.TaskType.STORY);
        return baseTaskCreateRequest;
    }

    private static LocalDateTime defaultStartDate() {
        return LocalDateTime.now();
    }

    private static LocalDateTime defaultDueDate(LocalDateTime start) {
        return start.plusDays(7);
    }
}
