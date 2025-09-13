package com.omori.taskmanagement.service.task;

import com.omori.taskmanagement.exceptions.task.InvalidTaskTypeException;
import com.omori.taskmanagement.exceptions.task.TaskNotFoundException;
import com.omori.taskmanagement.exceptions.task.TaskValidationException;
import com.omori.taskmanagement.model.project.Task;

import java.util.List;

public interface TaskHierarchyValidationService {

    /**
     * Validates the integrity of an Epic's complete hierarchy structure.
     *
     * <p>Performs comprehensive validation checks on the Epic and all its descendants:
     * <ul>
     *   <li>Hierarchy depth doesn't exceed maximum levels (Epic→Story→Task)</li>
     *   <li>No circular references exist in parent-child relationships</li>
     *   <li>All parent-child type combinations are valid</li>
     *   <li>Sort orders are consistent and without gaps</li>
     *   <li>No orphaned tasks exist in the hierarchy</li>
     * </ul></p>
     *
     * <p>This method is useful for data integrity checks and debugging
     * hierarchy corruption issues.</p>
     *
     * @param epicId the ID of the Epic task to validate hierarchy for
     * @throws TaskNotFoundException if no Epic task exists with the given ID
     * @throws TaskValidationException if any hierarchy validation rules are violated
     * @throws InvalidTaskTypeException if task type relationships are invalid
     * @since 1.0.0
     */
    void validateHierarchy(Long epicId);


    /**
     * Validates that a task's type is appropriate for its position in the hierarchy.
     *
     * <p>Performs comprehensive task type validation according to hierarchy rules:
     * <ul>
     *   <li><strong>EPIC tasks:</strong> Must be top-level (no parent) or have null parent</li>
     *   <li><strong>STORY tasks:</strong> Must have EPIC parent only</li>
     *   <li><strong>TASK tasks:</strong> Must have STORY parent only</li>
     * </ul></p>
     *
     * <h3>Validation Checks:</h3>
     * <ul>
     *   <li>Task type is not null and is valid enum value</li>
     *   <li>Parent-child type relationship follows business rules</li>
     *   <li>No invalid type combinations (e.g., TASK directly under EPIC)</li>
     *   <li>Referential integrity between task and its parent</li>
     * </ul>
     *
     * <h3>Hierarchy Rules Enforced:</h3>
     * <pre>
     * ✅ Valid:   EPIC (no parent)
     * ✅ Valid:   EPIC → STORY → TASK
     * ❌ Invalid: STORY (no parent)
     * ❌ Invalid: EPIC → TASK (skipped STORY level)
     * ❌ Invalid: STORY → EPIC (reverse hierarchy)
     * </pre>
     *
     * @param task the task to validate type and hierarchy position for
     * @throws TaskValidationException if task type violates hierarchy rules
     * @throws InvalidTaskTypeException if task type is invalid or null
     * @throws TaskNotFoundException if parent task referenced but doesn't exist
     * @since 1.0.0
     */
    void validateTaskType(Task task);

    /**
     * Validates sort order consistency and integrity for a collection of tasks.
     *
     * <p>Ensures sort order values maintain proper ordering and uniqueness within
     * each parent-child group. Tasks are grouped by their parent and validated
     * independently to maintain consistent ordering at each hierarchy level.</p>
     *
     * <h3>Validation Rules:</h3>
     * <ul>
     *   <li><strong>Uniqueness:</strong> No duplicate sort orders within same parent group</li>
     *   <li><strong>Non-negative:</strong> All sort order values must be ≥ 0</li>
     *   <li><strong>Consistency:</strong> Sort orders should form logical sequence</li>
     *   <li><strong>Completeness:</strong> No missing sort orders causing gaps</li>
     * </ul>
     *
     * <h3>Parent Grouping:</h3>
     * <p>Tasks are grouped by their parent task ID for validation:
     * <ul>
     *   <li>Top-level EPICs: grouped separately (parent = null)</li>
     *   <li>Stories under Epic: grouped by Epic ID</li>
     *   <li>Tasks under Story: grouped by Story ID</li>
     * </ul></p>
     *
     * <h3>Example Valid Sort Orders:</h3>
     * <pre>
     * Epic1 → Story1 (sort: 0), Story2 (sort: 1), Story3 (sort: 2) ✅
     * Story1 → Task1 (sort: 0), Task2 (sort: 1) ✅
     *
     * Epic1 → Story1 (sort: 0), Story2 (sort: 0) ❌ Duplicate
     * Story1 → Task1 (sort: -1) ❌ Negative value
     * Epic1 → Story1 (sort: 0), Story2 (sort: 5) ❌ Large gap
     * </pre>
     *
     * @param tasks the collection of tasks to validate sort orders for
     * @throws TaskValidationException if sort order violations are found
     * @throws IllegalArgumentException if tasks collection is null
     * @since 1.0.0
     */
    void validateSortOrder(List<Task> tasks);
}
