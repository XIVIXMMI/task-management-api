package com.omori.taskmanagement.service.task.utils;

import com.omori.taskmanagement.exceptions.UserNotFoundException;
import com.omori.taskmanagement.exceptions.task.TaskBusinessException;
import com.omori.taskmanagement.model.project.Category;
import com.omori.taskmanagement.model.project.Task;
import com.omori.taskmanagement.model.project.Workspace;
import com.omori.taskmanagement.model.usermgmt.User;
import com.omori.taskmanagement.repository.project.CategoryRepository;
import com.omori.taskmanagement.repository.project.WorkspaceRepository;
import com.omori.taskmanagement.repository.usermgmt.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskRelationsServiceImpl implements TaskRelationsService{

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final WorkspaceRepository workspaceRepository;

    public void setTaskRelations(Task task, Long categoryId, Long assignedToId, Long workspaceId) {
        if (categoryId != null) {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new TaskBusinessException("Category not found with id: " + categoryId));
            task.setCategory(category);
        }

        if (assignedToId != null) {
            User assignedTo = userRepository.findById(assignedToId)
                    .orElseThrow(() -> new UserNotFoundException("User not found with id: " + assignedToId));
            task.setAssignedTo(assignedTo);
        }

        if (workspaceId != null) {
            Workspace workspace = workspaceRepository.findById(workspaceId)
                    .orElseThrow(() -> new TaskBusinessException("Workspace not found with id: " + workspaceId));
            task.setWorkspace(workspace);
        }

    }
}
