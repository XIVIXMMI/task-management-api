package com.omori.taskmanagement.dto.project.task.creation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
public class StandaloneTaskRequest extends TaskCreateRequest{

    @Override
    @JsonIgnore
    public Long getParentId() {
        return null; // Always return null for a standalone task
    }

    @Override
    public void setParentId(Long parentId) {
        throw new IllegalArgumentException("Cannot set parentId for a standalone task");
    }
}
