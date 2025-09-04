package com.omori.taskmanagement.dto.project.task;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class StoryWithTaskDto {
    private TaskResponse story;
    private List<TaskResponse> tasks = new ArrayList<>();
}
