package com.omori.taskmanagement.dto.project.task.creation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;

public class StandaloneStoryRequest extends StoryCreateRequest{

    @Override
    @JsonIgnore
    @Schema(description = "Parent ID is not allow for standalone task")
    public Long getParentId() {
        return null; // Always return null for a standalone task
    }

    @Override
    public void setParentId(Long parentId) {
        throw new IllegalArgumentException("Cannot set parentId for a standalone task");
    }
}
