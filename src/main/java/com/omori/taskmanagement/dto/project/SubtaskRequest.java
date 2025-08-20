package com.omori.taskmanagement.dto.project;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@Builder
public class SubtaskRequest {

    /**
     *  Set by the controller from the path variable; not supplied in the request body.
     */
    private Long taskId;

    @NotBlank(message = "Title is required")
    @Schema( description = "Title of subtasks", example = "take notes")
    private String title;

    @Schema(description = "Description of the subtasks", example = "take notes on important parts")
    private String description;

    @Schema(description = "The value represents the position of the subtask in the list, used to sort the display order.", example = "1")
    private Integer sortOrder;
}
