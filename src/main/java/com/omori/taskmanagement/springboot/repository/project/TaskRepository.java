package com.omori.taskmanagement.springboot.repository.project;

import com.omori.taskmanagement.springboot.model.project.Task;
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

    // Find by UUID
    @Query("SELECT t FROM Task t WHERE t.uuid = :uuid AND t.deletedAt IS NULL")
    Optional<Task> findByUuid(@Param("uuid") UUID uuid);

    // Find by user - chỉ tasks không bị xóa
    @Query("SELECT t FROM Task t WHERE t.user.id = :userId AND t.deletedAt IS NULL")
    Page<Task> findByUserIdAndNotDeleted(@Param("userId") Long userId, Pageable pageable);

    // Find by status
    @Query("SELECT t FROM Task t WHERE t.user.id = :userId AND t.status = :status AND t.deletedAt IS NULL")
    List<Task> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") Task.TaskStatus status);

    // Find by priority
    @Query("SELECT t FROM Task t WHERE t.user.id = :userId AND t.priority = :priority AND t.deletedAt IS NULL")
    List<Task> findByUserIdAndPriority(@Param("userId") Long userId, @Param("priority") Task.TaskPriority priority);

    // Find overdue tasks - tìm các task đang còn hạn
    @Query("SELECT t FROM Task t WHERE t.user.id = :userId AND t.dueDate < :now AND t.status != 'completed' AND t.deletedAt IS NULL")
    List<Task> findOverdueTasksByUserId(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    // Find by workspace
    @Query("SELECT t FROM Task t WHERE t.workspace.id = :workspaceId AND t.deletedAt IS NULL")
    Page<Task> findByWorkspaceIdAndNotDeleted(@Param("workspaceId") Long workspaceId, Pageable pageable);

    // Search tasks by title or description
    @Query("SELECT t FROM Task t WHERE t.user.id = :userId AND " +
            "(LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "t.deletedAt IS NULL")
    Page<Task> searchTasksByKeyword(@Param("userId") Long userId, @Param("keyword") String keyword, Pageable pageable);
}