package com.omori.taskmanagement.dto.project.subtask;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class SubtaskCreateRequest {

    @NotBlank(message = "Title is required")
    @Schema( description = "Title of subtasks", example = "take notes")
    private String title;

    @Schema(description = "Description of the subtasks", example = "take notes on important parts")
    private String description;

    @Schema(description = "The value represents the position of the subtask in the list, used to sort the display order.", example = "1")
    private Integer sortOrder;
}