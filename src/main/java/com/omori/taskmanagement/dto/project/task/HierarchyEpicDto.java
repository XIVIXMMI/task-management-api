package com.omori.taskmanagement.dto.project.task;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class HierarchyEpicDto {
    private TaskResponse epic;
    private List<StoryWithTaskDto> stories = new ArrayList<>();
}
