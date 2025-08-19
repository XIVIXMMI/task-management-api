package com.omori.taskmanagement.service;

import com.omori.taskmanagement.dto.project.HierarchyEpicDto;
import com.omori.taskmanagement.dto.project.TaskCreateRequest;
import com.omori.taskmanagement.model.project.Subtask;
import com.omori.taskmanagement.model.project.Task;

import java.util.List;

public interface TaskHybridService {

    /**
     * Create and persists tasks for a specific type using provided data
     *
     * @param type      the type of task to create (e,g ... EPIC, STORY, TASK)
     * @param request   container for all required fields
     * @return the persisted Task entity
     */
    Task createEpicTask(Long userId, Task.TaskType type, TaskCreateRequest request);
    Task createStoryTask(Long userId, Task.TaskType type, TaskCreateRequest request);
    Task createTask(Long userId, Task.TaskType type, TaskCreateRequest request);
    List<Subtask> addSubtasksToTask(Long taskId, List<String> subtasksTitles);
    List<Task> getStoriesTaskByEpicId(Long epicTaskId);
    List<Task> getTasksByStoryId(Long storyTaskId);
    HierarchyEpicDto getFullHierarchy(Long epicId);
    void updateEpicTaskProgress(Long epicTaskId);
    void updateStoryTaskProgress(Long storyTaskId);
}