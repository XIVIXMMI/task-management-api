package com.omori.taskmanagement.dto.project.task.creation;

import com.omori.taskmanagement.model.project.Task;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
public class StoryCreateRequest extends BaseTaskCreateRequest {

//    @Schema(description = "Epic ID this story belongs to", example = "5")
//    private Long epicId;
//
//    @Schema(description = "Story points for estimation", example = "8")
//    private Integer storyPoints;
//
//    @Schema(description = "Acceptance criteria for this story")
//    private String acceptanceCriteria;

    // Override type to be STORY only
    @Override
    @Schema(description = "Task type (always STORY)", example = "STORY", allowableValues = {"STORY"})
    public Task.TaskType getType() {
        return Task.TaskType.STORY;
    }

    // Stories typically don't have:
    // - parentId (use epicId instead for clarity)
}
