package com.omori.taskmanagement.service.subtask;

import java.util.List;

import com.omori.taskmanagement.dto.project.subtask.SubtaskCreateRequest;
import com.omori.taskmanagement.dto.project.subtask.SubtaskRequest;
import com.omori.taskmanagement.dto.project.subtask.SubtaskUpdateRequest;
import com.omori.taskmanagement.exceptions.task.TaskNotFoundException;
import com.omori.taskmanagement.exceptions.task.TaskValidationException;
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
     * Creates multiple subtasks for a task in a single operation.
     *
     * @param taskId the ID of the task to add subtasks to
     * @param subtaskTitles the list of subtask titles to create
     * @return the list of created subtasks
     * @throws TaskNotFoundException if no task exists with the given ID
     * @throws TaskValidationException if any subtask title is null or empty
     */
    List<Subtask> addSubtasksToTask(Long taskId, List<String> subtaskTitles);

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
