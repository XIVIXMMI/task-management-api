package com.omori.taskmanagement.service.task.hierarchy;

import com.omori.taskmanagement.dto.project.task.HierarchyEpicDto;
import com.omori.taskmanagement.dto.project.task.TaskResponse;
import com.omori.taskmanagement.exceptions.task.InvalidTaskTypeException;
import com.omori.taskmanagement.exceptions.task.TaskNotFoundException;
import com.omori.taskmanagement.exceptions.task.TaskValidationException;
import com.omori.taskmanagement.model.project.Task;

import java.util.List;

/**
 * Service for managing task hierarchy operations including Epic-Story-Task relationships.
 *
 * <p>This service handles the three-level task hierarchy:
 * <ul>
 *   <li><strong>EPIC (Level 0)</strong> - Can contain STORY tasks</li>
 *   <li><strong>STORY (Level 1)</strong> - Can contain TASK tasks</li>
 *   <li><strong>TASK (Level 2)</strong> - Leaf level with subtasks</li>
 * </ul></p>
 *
 * @since 1.0.0
 */
public interface TaskHierarchyService {

    /**
     * Retrieves the complete hierarchy structure for an Epic task by ID.
     *
     * <p>Returns a comprehensive view of the Epic including all its child Stories,
     * and all Tasks under those Stories, along with their subtasks. This method
     * performs optimized queries to load the entire hierarchy efficiently.</p>
     *
     * <h3>Hierarchy Structure Returned:</h3>
     * <pre>
     * Epic (with subtasks)
     * ├── Story 1 (with subtasks)
     * │   ├── Task 1.1 (with subtasks)
     * │   └── Task 1.2 (with subtasks)
     * └── Story 2 (with subtasks)
     *     ├── Task 2.1 (with subtasks)
     *     └── Task 2.2 (with subtasks)
     * </pre>
     *
     * @param epicId the ID of the Epic task to retrieve hierarchy for
     * @return complete hierarchy structure with Epic, Stories, Tasks, and Subtasks
     * @throws TaskNotFoundException if no Epic task exists with the given ID
     * @throws TaskValidationException if the task with given ID is not an EPIC type
     * @since 1.0.0
     */
    HierarchyEpicDto getFullHierarchy(Long epicId);

    /**
     * Retrieves the complete hierarchy structure for an Epic task by UUID.
     *
     * <p>Same functionality as {@link #getFullHierarchy(Long)} but uses UUID
     * for identification. Useful for external API calls where UUIDs are preferred
     * over internal database IDs for security and consistency.</p>
     *
     * @param uuid the UUID of the Epic task to retrieve hierarchy for
     * @return complete hierarchy structure with Epic, Stories, Tasks, and Subtasks
     * @throws TaskNotFoundException if no Epic task exists with the given UUID
     * @throws TaskValidationException if the UUID format is invalid or task is not EPIC type
     * @since 1.0.0
     * @see #getFullHierarchy(Long)
     */
    HierarchyEpicDto getFullHierarchyByUuid(String uuid);

    /**
     * Retrieves the direct child tasks of a parent task.
     *
     * <p>Returns only immediate children (one level down) of the specified parent.
     * For example:
     * <ul>
     *   <li>If parent is EPIC → returns STORY tasks</li>
     *   <li>If parent is STORY → returns TASK tasks</li>
     *   <li>If parent is TASK → returns empty list (TASKs have subtasks, not child tasks)</li>
     * </ul></p>
     *
     * @param parentTaskId the ID of the parent task
     * @return list of direct child tasks, empty if no children exist
     * @throws TaskNotFoundException if no task exists with the given parent ID
     * @since 1.0.0
     */
    List<Task> getChildTasks(Long parentTaskId);

    /**
     * Retrieves all descendant tasks of a parent task recursively.
     *
     * <p>Returns all tasks in the hierarchy below the specified parent, regardless
     * of depth. For an Epic, this includes both Stories and Tasks under those Stories.</p>
     *
     * <h3>Example for Epic parent:</h3>
     * <pre>
     * Returns: [Story1, Story2, Task1.1, Task1.2, Task2.1, Task2.2]
     * </pre>
     *
     * @param parentTaskId the ID of the parent task
     * @return list of all descendant tasks (flattened hierarchy), empty if no descendants
     * @throws TaskNotFoundException if no task exists with the given parent ID
     * @since 1.0.0
     */
    List<Task> getAllChildTasks(Long parentTaskId);

    /**
     * Retrieves the parent task of the specified task.
     *
     * <p>Returns the immediate parent in the hierarchy:
     * <ul>
     *   <li>TASK → returns its STORY parent</li>
     *   <li>STORY → returns its EPIC parent</li>
     *   <li>EPIC → returns null (top level)</li>
     * </ul></p>
     *
     * @param taskId the ID of the task to find parent for
     * @return the parent task, or null if task is at top level (Epic) or standalone
     * @throws TaskNotFoundException if no task exists with the given ID
     * @since 1.0.0
     */
    TaskResponse getParentTask(Long taskId);

    /**
     * Moves a task to a new parent, maintaining hierarchy rules.
     *
     * <p>Validates and updates the parent-child relationship while ensuring
     * hierarchy constraints are maintained:
     * <ul>
     *   <li>STORY tasks can only be moved under EPIC parents</li>
     *   <li>TASK tasks can only be moved under STORY parents</li>
     *   <li>Circular references are prevented</li>
     *   <li>Sort order is automatically assigned</li>
     * </ul></p>
     *
     * <p><strong>Side Effects:</strong></p>
     * <ul>
     *   <li>Updates task's parentTask relationship</li>
     *   <li>Assigns new sort order under new parent</li>
     *   <li>May trigger progress recalculation for both old and new parents</li>
     * </ul>
     *
     * @param taskId the ID of the task to move
     * @param newParentId the ID of the new parent task, or null for top-level
     * @throws TaskNotFoundException if task or new parent doesn't exist
     * @throws InvalidTaskTypeException if hierarchy rules would be violated
     * @throws TaskValidationException if move would create circular reference
     * @since 1.0.0
     */
    void moveTaskToParent(Long taskId, Long newParentId);

    /**
     * Calculates the depth of a task within the hierarchy structure.
     *
     * <p>Returns the hierarchical depth level:
     * <ul>
     *   <li><strong>0</strong> - EPIC (top level)</li>
     *   <li><strong>1</strong> - STORY (under Epic)</li>
     *   <li><strong>2</strong> - TASK (under Story)</li>
     * </ul></p>
     *
     * <p>Standalone tasks (without parents) return their natural depth based on type.</p>
     *
     * @param taskId the ID of the task to calculate depth for
     * @return the depth level (0-2), where 0 is top level
     * @throws TaskNotFoundException if no task exists with the given ID
     * @since 1.0.0
     */
    int getHierarchyDepth(Long taskId);

    /**
     * Calculates the next available sort order for tasks under a parent.
     *
     * <p>Determines the appropriate sort order value for a new task being
     * added to a parent. This ensures consistent ordering and prevents
     * conflicts with existing child tasks.</p>
     *
     * <p>The sort order is calculated as: {@code max(existing_sort_orders) + 1}</p>
     *
     * @param parentTaskId the ID of the parent task to calculate sort order for
     * @return the next available sort order value, starting from 0 if no children exist
     * @throws TaskNotFoundException if no parent task exists with the given ID
     * @since 1.0.0
     */
    Integer getNextSortOrderForParent(Long parentTaskId);

}
