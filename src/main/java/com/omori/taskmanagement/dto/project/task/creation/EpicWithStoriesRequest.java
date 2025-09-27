package com.omori.taskmanagement.dto.project.task.creation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class EpicWithStoriesRequest extends EpicCreateRequest {

    @Valid
    @ToString.Exclude
    @Size(max = 10, message = "Cannot create more than 10 initial stories")
    @Schema(description = "Initial stories under this epic task (simplified format)",
            example = "[{\"title\":\"story 1\",\n\"description\":\"Do task A\"},{\"title\":\"story 2\",\n\"description\":\"Do task B\"}]")
    private List<InitialStoryRequest> initialStories; // for createEpicWithInitialStories
}
