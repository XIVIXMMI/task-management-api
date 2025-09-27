package com.omori.taskmanagement.dto.project.task.creation;

import com.omori.taskmanagement.model.project.Task;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
public class TaskCreateRequest extends BaseTaskCreateRequest {

//    @Schema(description = "Story ID this task belongs to", example = "12")
//    private Long storyId;
//
//    @Schema(description = "Progress percentage (0-100)", example = "25")
//    private Integer progress;

    // Override type to be TASK only
    @Override
    @Schema(description = "Task type (always TASK)", example = "TASK", allowableValues = {"TASK"})
    public Task.TaskType getType() {
        return Task.TaskType.TASK;
    }

    // Tasks use all base properties since they're the most granular level
}
