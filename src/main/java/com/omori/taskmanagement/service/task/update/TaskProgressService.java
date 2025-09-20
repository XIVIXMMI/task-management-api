package com.omori.taskmanagement.service.task.update;

import com.omori.taskmanagement.exceptions.task.TaskBusinessException;
import com.omori.taskmanagement.exceptions.task.TaskNotFoundException;
import com.omori.taskmanagement.exceptions.task.TaskValidationException;

/**
 * Service for managing task progress calculations and propagation across the hierarchy.
 *
 * <p>This service handles progress computation and synchronization for all levels of the
 * task hierarchy (Epic → Story → Task → Subtasks). Progress values are calculated as
 * percentages (0-100) and automatically propagated up the hierarchy when child progress changes.</p>
 *
 * <h3>Progress Calculation Rules:</h3>
 * <ul>
 *   <li><strong>Task Progress:</strong> Based on completed subtasks percentage</li>
 *   <li><strong>Story Progress:</strong> Weighted average of own subtasks (50%) + child tasks (50%)</li>
 *   <li><strong>Epic Progress:</strong> Weighted average of own subtasks (50%) + child stories (50%)</li>
 * </ul>
 *
 * <h3>Hierarchy Progress Flow:</h3>
 * <pre>
 * Subtask Completed → Task Progress → Story Progress → Epic Progress
 * </pre>
 *
 * @since 1.0.0
 */
public interface TaskProgressService {

    /**
     * Calculates the progress percentage for a task based on its completed subtasks.
     *
     * <p>Computes the progress as the percentage of completed subtasks relative to total subtasks.
     * For tasks without subtasks, returns 0. The calculation is:
     * {@code (completed_subtasks / total_subtasks) * 100}</p>
     *
     * <p><strong>Note:</strong> This method only calculates progress from direct subtasks
     * and does not consider child tasks in the hierarchy. For hierarchy-aware progress
     * calculation, use other methods in this service.</p>
     *
     * @param taskId the ID of the task to calculate progress for
     * @return the progress percentage (0-100), or 0 if no subtasks exist
     * @throws TaskNotFoundException if no task exists with the given ID
     * @since 1.0.0
     * @see #updateProgressFromSubtasks(Long)
     */
    int calculateTaskProgress(Long taskId);

    /**
     * Updates a task's progress based on its subtask completion and propagates changes upward.
     *
     * <p>Recalculates the task's progress percentage based on completed subtasks and updates
     * the task entity in the database. After updating the task's own progress, this method
     * automatically triggers progress updates for parent tasks up the hierarchy.</p>
     *
     * <p><strong>Cascade Behavior:</strong></p>
     * <ul>
     *   <li>Updates the target task's progress from its subtasks</li>
     *   <li>If task has a STORY parent → triggers {@link #propagateProgressToParent(Long)}</li>
     *   <li>If task has an EPIC parent → triggers {@link #updateHierarchyProgress(Long)}</li>
     * </ul>
     *
     * <p><strong>Transaction:</strong> This operation is transactional and will rollback
     * all changes if any step in the cascade fails.</p>
     *
     * @param taskId the ID of the task to update progress for
     * @throws TaskNotFoundException if no task exists with the given ID
     * @throws TaskBusinessException if the progress update operation fails
     * @since 1.0.0
     * @see #calculateTaskProgress(Long)
     * @see #propagateProgressToParent(Long)
     */
    void updateProgressFromSubtasks(Long taskId);

    /**
     * Propagates progress changes from a task to its parent and continues up the hierarchy.
     *
     * <p>Updates the parent task's progress using a weighted calculation that considers both
     * the parent's own subtask progress and the average progress of all its child tasks.
     * The weighting formula is: {@code (own_subtasks_progress * 0.5) + (children_average_progress * 0.5)}</p>
     *
     * <p><strong>Propagation Chain:</strong></p>
     * <ol>
     *   <li>Calculate parent's own subtask progress (0-100%)</li>
     *   <li>Calculate average progress of all child tasks</li>
     *   <li>Apply 50/50 weighted formula</li>
     *   <li>Update parent task's progress in database</li>
     *   <li>If parent has its own parent, continue propagation upward</li>
     * </ol>
     *
     * <p>This method is typically called automatically by {@link #updateProgressFromSubtasks(Long)}
     * but can also be invoked directly when manual progress recalculation is needed.</p>
     *
     * @param taskId the ID of the task whose parent progress should be updated
     * @throws TaskNotFoundException if no task exists with the given ID
     * @throws TaskBusinessException if the parent progress update fails
     * @since 1.0.0
     * @see #updateProgressFromSubtasks(Long)
     * @see #updateHierarchyProgress(Long)
     */
    void propagateProgressToParent(Long taskId);

    /**
     * Recalculates and updates progress for an entire Epic hierarchy from top-down.
     *
     * <p>Performs a comprehensive progress update starting from the specified Epic task,
     * recalculating progress for all levels of the hierarchy. This method is useful for
     * data consistency maintenance and bulk progress corrections.</p>
     *
     * <p><strong>Update Process:</strong></p>
     * <ol>
     *   <li>Validates that the specified task is an EPIC type</li>
     *   <li>Calculates Epic's own progress from its subtasks</li>
     *   <li>Retrieves all child Story tasks under the Epic</li>
     *   <li>Calculates average progress of all child Stories</li>
     *   <li>Applies weighted formula: (epic_subtasks * 50%) + (stories_average * 50%)</li>
     *   <li>Updates the Epic's progress in database</li>
     * </ol>
     *
     * <p><strong>Performance Note:</strong> This operation may be expensive for large
     * hierarchies as it processes multiple database queries and calculations. Use with
     * caution in high-frequency scenarios.</p>
     *
     * @param epicId the ID of the Epic task to update hierarchy progress for
     * @throws TaskNotFoundException if no Epic task exists with the given ID
     * @throws TaskValidationException if the task with given ID is not an EPIC type
     * @throws TaskBusinessException if the hierarchy progress update fails
     * @since 1.0.0
     * @see #propagateProgressToParent(Long)
     */
    void updateHierarchyProgress(Long epicId);
}
