package com.omori.taskmanagement.springboot.dto.project;

import com.omori.taskmanagement.springboot.model.project.Task;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class StoryWithTaskDto {
    private TaskResponse story;
    private List<Task> tasks = new ArrayList<>();
}
