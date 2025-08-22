package com.omori.taskmanagement.repository.project;

import com.omori.taskmanagement.model.project.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, Long> {

        /**
         * Finds a task by UUID.
         * Performance: O(1) lookup using unique UUID index
         * Use case: Public API access, URL-based task sharing
         *
         * @param uuid the unique task identifier
         * @return task, if found and not deleted, empty otherwise
         */
        @Query("SELECT t FROM Task t WHERE t.uuid = :uuid AND t.deletedAt IS NULL")
        Optional<Task> findByUuid(@Param("uuid") UUID uuid);

        /**
         * Retrieves user's tasks with pagination.
         * Performance: Composite index on (user_id, deleted_at), efficient pagination
         * Use case: Task dashboard, user's main task list
         *
         * @param userId the task owner identifier
         * @param pageable pagination and sorting parameters
         * @return paginated list of user's active tasks
         */
        @Query("SELECT t FROM Task t WHERE t.user.id = :userId AND t.deletedAt IS NULL")
        Page<Task> findByUserIdAndNotDeleted(@Param("userId") Long userId, Pageable pageable);

        /**
         * Finds user's tasks by status.
         * Performance: Composite index on (user_id, status, deleted_at)
         * Use case: Kanban board columns, status filtering
         *
         * @param userId the task owner identifier
         * @param status the task status to filter by
         * @return list of matching tasks
         */
        @Query("SELECT t FROM Task t WHERE t.user.id = :userId AND t.status = :status AND t.deletedAt IS NULL")
        List<Task> findByUserIdAndStatus(@Param("userId") Long userId,
                                         @Param("status") Task.TaskStatus status);

        /**
         * Finds user's tasks by priority level.
         * Performance: Composite index on (user_id, priority, deleted_at)
         * Use case: Priority-based task filtering, urgent task views
         *
         * @param userId the task owner identifier
         * @param priority the priority level to filter by
         * @return list of tasks with a specified priority
         */
        @Query("SELECT t FROM Task t WHERE t.user.id = :userId " +
                "AND t.priority = :priority AND t.deletedAt IS NULL")
        List<Task> findByUserIdAndPriority(@Param("userId") Long userId, @Param("priority") Task.TaskPriority priority);

        /**
         * Finds overdue incomplete tasks.
         * Performance: Composite index on (user_id, due_date, status, deleted_at)
         * Use case: Overdue notifications, deadline alerts, task reminders
         *
         * @param userId the task owner identifier
         * @param now current timestamp for comparison
         * @return list of overdue tasks requiring attention
         */
        @Query("SELECT t FROM Task t " +
                "WHERE t.user.id = :userId AND t.dueDate < :now AND t.status != 'completed' AND t.deletedAt IS NULL")
        List<Task> findOverdueTasksByUserId(@Param("userId") Long userId, @Param("now") LocalDateTime now);

        /**
         * Retrieves workspace tasks with pagination.
         * Performance: Composite index on (workspace_id, deleted_at)
         * Use case: Team workspace views, collaborative task management
         *
         * @param workspaceId the workspace identifier
         * @param pageable pagination and sorting parameters
         * @return paginated list of workspace tasks
         */
        @Query("SELECT t FROM Task t WHERE t.workspace.id = :workspaceId AND t.deletedAt IS NULL")
        Page<Task> findByWorkspaceIdAndNotDeleted(@Param("workspaceId") Long workspaceId, Pageable pageable);

        /**
         * Search tasks by keyword in the title / description.
         * Performance: Case-insensitive LIKE queries, consider full-text search for large datasets
         * Use case: Global search, task discovery, finding specific tasks
         *
         * @param userId the task owner identifier
         * @param keyword search term to match in title/description
         * @param pageable pagination and sorting parameters
         * @return paginated search results
         */
        @Query("SELECT t FROM Task t WHERE t.user.id = :userId AND " +
                "(LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                "LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
                "t.deletedAt IS NULL")
        Page<Task> searchTasksByKeyword(@Param("userId") Long userId,
                                        @Param("keyword") String keyword,
                                        Pageable pageable);

        /**
         * Finds a task by ID with eager loading of relationships.
         * Performance: Single query with LEFT JOIN FETCH prevents N+1 queries
         * Use case: Task detail pages, task editing forms, API responses with full data
         *
         * @param taskId task identifier
         * @return task with all relations fetched, or empty if not found
         */
        @Query("SELECT t FROM Task t " +
                "LEFT JOIN FETCH t.category " +
                "LEFT JOIN FETCH t.assignedTo " +
                "LEFT JOIN FETCH t.workspace " +
                "LEFT JOIN FETCH t.user " +
                "WHERE t.id = :taskId")
        Optional<Task> findByIdWithRelations(@Param("taskId") Long taskId);

        /**
         * Finds all direct child tasks with proper ordering.
         * Performance: Index on (parent_task_id, deleted_at, sort_order)
         * Use case: Subtask listings, hierarchical task displays
         *
         * @param parentTaskId the parent task identifier
         * @return list of child tasks ordered by sort_order
         */
        @Query("SELECT t FROM Task t WHERE t.parentTask.id = :parentTaskId AND t.deletedAt IS NULL ORDER BY t.sortOrder")
        List<Task> findByParentTaskIdAndDeletedAtIsNull(@Param("parentTaskId") Long parentTaskId);

        /**
         * Finds child tasks of a specific type under the parent.
         * Performance: Composite index on (parent_task_id, task_type, deleted_at)
         * Use case: Epic-Story relationships, Story-Task relationships, hierarchy management
         *
         * @param parentTaskId the parent task identifier
         * @param taskType the specific task type to filter (EPIC/STORY/TASK)
         * @return list of matching child tasks
         */
        @Query("SELECT t FROM Task t " +
                "WHERE t.parentTask.id = :parentTaskId AND t.taskType = :taskType AND t.deletedAt IS NULL ORDER BY t.sortOrder")
        List<Task> findByParentTaskIdAndTaskTypeAndDeletedAtIsNull(@Param("parentTaskId") Long parentTaskId,
                                                                   @Param("taskType") Task.TaskType taskType);

        @Query("SELECT t FROM Task t " +
                "LEFT JOIN FETCH t.parentTask " +
                "WHERE (" +
                "t.uuid = :epicUuid OR " +
                "t.parentTask.uuid = :epicUuid OR " +
                "t.parentTask.uuid IN (SELECT s.uuid FROM Task s WHERE s.parentTask.uuid = :epicUuid AND s.deletedAt IS NULL) " +
                ") AND t.deletedAt IS NULL ORDER BY t.sortOrder")
        List<Task> findAllTasksUnderEpicByUuid(@Param("epicUuid") UUID epicUuid);

        /**
         * Finds all tasks in epic hierarchy (flattened view).
         * Performance: Subquery with IN clause, may be expensive for deep hierarchies
         * Use case: Epic progress calculation, bulk operations on epic scope
         *
         * @param epicId the epic task identifier
         * @return flattened list of all descendant tasks
         */
        @Query("SELECT t FROM Task t WHERE (" +
                "t.id = :epicId OR " +
                "t.parentTask.id = :epicId OR " +
                "t.parentTask.id IN (SELECT s.id FROM Task s WHERE s.parentTask.id = :epicId)" +
                ") AND t.deletedAt IS NULL ORDER BY t.sortOrder")
        List<Task> findAllTasksUnderEpic(@Param("epicId") Long epicId);

        /**
         * Finds maximum sort order for new task positioning.
         * Performance: MAX() aggregate with proper indexing is very fast
         * Use case: Adding new tasks to the end of a list, maintaining sort order
         *
         * @param parentTaskId the parent task identifier
         * @return highest sort_order value, empty if no children exist
         */
        @Query("SELECT MAX(t.sortOrder) FROM Task t WHERE t.parentTask.id = :parentTaskId AND t.deletedAt IS NULL")
        Optional<Integer> findMaxSortOrderByParentTaskId(@Param("parentTaskId") Long parentTaskId);

        /**
         * Counts the total number of active subtasks for a given task.
         * Performance: COUNT() aggregate with proper indexing is very fast
         * Use case: Determining subtask count for progress calculations and UI display
         *
         * @param taskId the parent task identifier
         * @return total count of non-deleted subtasks, 0 if no subtasks exist
         */
        @Query("SELECT COUNT(s) FROM Subtask s WHERE s.task.id = :taskId AND s.deletedAt IS NULL")
        Long countSubtasksByTaskId(@Param("taskId") Long taskId);

        @Query("SELECT COUNT(s) FROM Subtask s WHERE s.task.id = :taskId AND s.isCompleted = true AND s.deletedAt IS NULL")
        Long countCompletedSubtasksByTaskId(@Param("taskId") Long taskId);

}