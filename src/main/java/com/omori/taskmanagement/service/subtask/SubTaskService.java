package com.omori.taskmanagement.service.subtask;

import java.util.List;

import com.omori.taskmanagement.dto.project.SubtaskCreateRequest;
import com.omori.taskmanagement.dto.project.SubtaskRequest;
import com.omori.taskmanagement.dto.project.SubtaskUpdateRequest;
import com.omori.taskmanagement.model.project.Subtask;


public interface SubTaskService {


    /**
     * Creates a new subtask with the specified title and associates it with a task.
     *
     * @param subtask the subtask to create
     * @param taskId  the ID of the task to associate with the subtask
     * @param title   the title of the subtask
     */
    void createSubtask(SubtaskCreateRequest subtask, Long taskId, String title);
    
    /**
     * Creates a new subtask with all properties.
     *
     * @param request the subtask to create
     * @return the created subtask
     */
    Subtask createSubtask(SubtaskRequest request);

    /**
     * Updates an existing subtask.
     *
     * @param subtaskId the subtask to update
     * @return the updated subtask
     */
    Subtask updateSubtask(Long subtaskId, SubtaskUpdateRequest request);

    /**
     * Toggles the completion status of a subtask.
     *
     * @param subtaskId the ID of the subtask to toggle
     * @return the updated subtask with toggled completion status
     */
    Subtask toggleSubtaskCompletion(Long subtaskId);

    /**
     * Retrieves a subtask by its ID.
     *
     * @param taskId the ID of the subtask to retrieve
     * @return the retrieved subtask, or null if not found
     */
    List<Subtask> getSubtasksByTaskId(Long taskId);

    /**
     * Retrieves a subtask by its ID.
     *
     * @param taskId the ID of the subtask to retrieve
     * @return the retrieved subtask, or null if not found
     */
    List<Subtask> reorderSubtasks(Long taskId, List<Long> subtaskIds);

    /**
     * Retrieves a subtask by its ID.
     *
     * @param taskId the ID of the subtask to retrieve
     * @return the retrieved subtask, or null if not found
     */
    Integer getNextSortOrder(Long taskId);

    void deleteSubtask(Long subtaskId);

    void softDeleteSubtask(Long subtaskId);
}
