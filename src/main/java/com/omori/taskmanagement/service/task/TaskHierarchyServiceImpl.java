package com.omori.taskmanagement.service.task;

import com.omori.taskmanagement.dto.project.HierarchyEpicDto;
import com.omori.taskmanagement.model.project.Task;
import com.omori.taskmanagement.repository.project.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class TaskHierarchyServiceImpl implements TaskHierarchyService{

    private final TaskRepository taskRepository;

    @Override
    public HierarchyEpicDto getFullHierarchy(Long epicId) {
        return null;
    }

    @Override
    public HierarchyEpicDto getFullHierarchyByUuid(String uuid) {
        return null;
    }

    @Override
    public HierarchyEpicDto getChildTask(Long epicId) {
        return null;
    }

    @Override
    public void moveTaskToParent(Long taskId, Long parentId) {

    }

    @Override
    public void validateHierarchy(Long epicId) {

    }

    @Override
    public List<Task> getDirectChildren(Long parentTaskId) {
        return List.of();
    }

    @Override
    public List<Task> getAllDescendants(Long parentTaskId) {
        return List.of();
    }

    @Override
    public Task getParentTask(Long taskId) {
        return null;
    }

    @Override
    public int getHierarchyDepth(Long taskId) {
        return 0;
    }

    @Override
    public Integer getNextSortOrderForParent(Long parentTaskId) {
        // Implementation to get next sort order
        return taskRepository.findMaxSortOrderByParentTaskId(parentTaskId)
                .map(max -> max + 1)
                .orElse(0);
    }
}
