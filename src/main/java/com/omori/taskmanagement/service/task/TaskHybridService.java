package com.omori.taskmanagement.service.task;

import com.omori.taskmanagement.dto.project.task.HierarchyEpicDto;
import com.omori.taskmanagement.dto.project.task.TaskCreateRequest;
import com.omori.taskmanagement.model.project.Subtask;
import com.omori.taskmanagement.model.project.Task;

import java.util.List;

public interface TaskHybridService {

    // CreationService
    Task createEpicTask(Long userId, Task.TaskType type, TaskCreateRequest request);
    Task createStoryTask(Long userId, Task.TaskType type, TaskCreateRequest request);
    Task createTask(Long userId, Task.TaskType type, TaskCreateRequest request);

    List<Subtask> addSubtasksToTask(Long taskId, List<String> subtasksTitles);
    List<Task> getStoriesTaskByEpicId(Long epicTaskId);
    List<Task> getTasksByStoryId(Long storyTaskId);
    HierarchyEpicDto getFullHierarchy(Long epicId);
    HierarchyEpicDto getFullHierarchyByUuid(String uuid);

    // ProgressService
    void updateEpicTaskProgress(Long epicTaskId);
    void updateStoryTaskProgress(Long storyTaskId);
    void updateTaskProgressFromSubtasks(Long taskId);

}