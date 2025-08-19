package com.omori.taskmanagement.springboot.repository.project;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.stereotype.Repository;

import com.omori.taskmanagement.springboot.model.project.Subtask;

@Repository
public interface SubtaskRepository extends JpaRepository<Subtask, Long> {

    /**
     * Finds all subtasks by task ID and filters out deleted subtasks.
     *
     * @param  taskId the ID of the task
     * @return a list of non-deleted subtasks associated with the given task ID,
     *         ordered by sort order
     */
    List<Subtask> findByTaskIdAndDeletedAtIsNullOrderBySortOrder(Long taskId);

    /**
     * Finds a subtask by its ID and filters out deleted subtasks.
     *
     * @param id the ID of the subtask
     * @return an optional containing the non-deleted subtask if found, or empty if
     *         not found
     */
    Optional<Subtask> findByIdAndDeletedAtIsNull(Long id);

    /**
     * Count all incomplete subtasks by task ID.
     *
     * @param taskId the ID of the task
     * @return the count of non-deleted, incomplete subtasks associated with the
     *         given task ID
     */
    @Query("SELECT COUNT(s) FROM Subtask s WHERE s.task.id = :taskId AND s.isCompleted = false AND s.deletedAt IS NULL")
    Long countIncompletedSubtaskByTaskId(@Param("taskId") Long taskId);

    /**
     * Count all subtasks by task ID.
     *
     * @param taskId the ID of the task
     * @return the count of non-deleted subtasks associated with the given task ID
     */
    @Query("SELECT COUNT(s) FROM Subtask s WHERE s.task.id = :taskId AND s.deletedAt IS NULL")
    Long countSubtaskByTaskId(@Param("taskId") Long taskId);

    /**
     * Finds the maximum sort order of subtasks for a given task ID.
     *
     * @param taskId the ID of the task
     * @return the maximum sort order of non-deleted subtasks associated with the
     *         given task ID, or null if no subtasks exist
     */
    @Query("SELECT MAX(s.sortOrder) FROM Subtask s WHERE s.task.id = :taskId AND s.deletedAt IS NULL")
    Long findMaxSortOrderByTaskId(@Param("taskId") Long taskId);

    /**
     * Finds all completed subtasks by task ID and filters out deleted subtasks.
     *
     * @param taskId the ID of the task
     * @return a list of completed, non-deleted subtasks associated with the given
     *         task ID, ordered by sort order
     */
    List<Subtask> findByTaskIdAndIsCompletedTrueAndDeletedAtIsNullOrderBySortOrder(Long taskId);

    /**
     * Finds all incomplete subtasks by task ID and filters out deleted subtasks.
     *
     * @param taskId the ID of the task
     * @return a list of incomplete, non-deleted subtasks associated with the given
     *         task ID, ordered by sort order
     */
    List<Subtask> findByTaskIdAndIsCompletedFalseAndDeletedAtIsNullOrderBySortOrder(Long taskId);

    /**
     * Finds subtasks by task ID and title, ignoring case, and filters out deleted
     * subtasks.
     *
     * @param taskId the ID of the task
     * @param title  the title to search for
     * @return a list of non-deleted subtasks associated with the given task ID and
     *         title, ordered by sort order
     */
    List<Subtask> findByTaskIdAndTitleContainingIgnoreCaseAndDeletedAtIsNull(Long taskId, String title);

    /**
     * Calculates the completion percentage of subtasks for a given task ID.
     *
     * @param taskId the ID of the task
     * @return the completion percentage of non-deleted subtasks associated with
     *         the given task ID, or 0 if no subtasks exist
     * @eg we have 10 subtasks, 6 completed, 4 not completed, then the completion percentage is 60%
     */
    @Query("SELECT CASE WHEN COUNT(s) = 0 THEN 0 ELSE (COUNT(CASE WHEN s.isCompleted = true THEN 1 END) * 100.0 / COUNT(s)) END "
            +
            "FROM Subtask s WHERE s.task.id = :taskId AND s.deletedAt IS NULL")
    Double calculateCompletionPercentageByTaskId(@Param("taskId") Long taskId);

    /**
     * Deletes all subtasks associated with a given task ID.
     *
     * @param id the ID of the task
     */
    void deleteByTaskId(Long id);

    /**
     * Bulk load subtasks for multiple tasks efficiently.
     * Performance: Single query instead of N queries
     */
    @Query("SELECT s FROM Subtask s WHERE s.task.id IN :taskIds AND s.deletedAt IS NULL ORDER BY s.task.id, s.sortOrder")
    List<Subtask> findByTaskIdInAndDeletedAtIsNull(@Param("taskIds") List<Long> taskIds);
}
