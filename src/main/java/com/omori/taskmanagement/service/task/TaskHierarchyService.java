package com.omori.taskmanagement.service.task;

import com.omori.taskmanagement.dto.project.HierarchyEpicDto;
import com.omori.taskmanagement.model.project.Task;

import java.util.List;

public interface TaskHierarchyService {

    HierarchyEpicDto getFullHierarchy(Long epicId);
    HierarchyEpicDto getFullHierarchyByUuid(String uuid);
    HierarchyEpicDto getChildTask(Long epicId);
    void moveTaskToParent(Long taskId, Long parentId);
    void validateHierarchy(Long epicId);
    List<Task> getDirectChildren(Long parentTaskId);
    List<Task> getAllDescendants(Long parentTaskId);
    Task getParentTask(Long taskId);
    int getHierarchyDepth(Long taskId);
    Integer getNextSortOrderForParent(Long parentTaskId);

}
