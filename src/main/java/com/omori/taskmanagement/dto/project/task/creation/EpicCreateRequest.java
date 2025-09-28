package com.omori.taskmanagement.dto.project.task.creation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.omori.taskmanagement.model.project.Task;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class EpicCreateRequest extends BaseTaskCreateRequest{

    /* Epics typically don't have:
    - assignedToId (teams work on epics, not individuals)
    - estimatedHours (too high-level for time estimates)
    - parentId (epics are top-level)
     */

    @Override
    @Schema(description = "Task type (always EPIC)", example = "EPIC", allowableValues = {"EPIC"})
    public Task.TaskType getType() {
        return Task.TaskType.EPIC;
    }

    @Override
    @JsonIgnore
    public  Long getAssignedToId() {
        return null; // Always return null for epics
    }

    @Override
    public void setAssignedToId(Long assignedToId) {
        if (assignedToId != null) {
            throw new IllegalArgumentException("Cannot set assignedToId for an epic");
        }
    }

    @Override
    @JsonIgnore
    public Double getEstimatedHours() {
        return null;
    }

    @Override
    @JsonIgnore
    public Long getParentId() {
        return null;
    }

    @Override
    public void setParentId(Long parentId) {
        if (parentId != null) {
            throw new IllegalArgumentException("Cannot set parentId for an epic");
        }
    }


}
