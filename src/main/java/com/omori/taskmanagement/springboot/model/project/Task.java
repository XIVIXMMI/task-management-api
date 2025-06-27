package com.omori.taskmanagement.springboot.model.project;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@SuperBuilder
@Entity
@Table(name = "tasks", schema = "project")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "task_id")
    private Long id;

    @Column(nullable = false, unique = true)
    private java.util.UUID uuid;

    @Column(nullable = false)
    private String title;

    private String description;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "priority", columnDefinition = "task_priority DEFAULT 'medium'")
    private TaskPriority priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "status", columnDefinition = "task_status DEFAULT 'pending'")
    private TaskStatus status;

    @Column(name = "estimated_hours")
    private Double estimatedHours;

    @Column(name = "actual_hours")
    private Double actualHours;

    @Column(nullable = false, name = "progress")
    private Integer progress;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private com.omori.taskmanagement.springboot.model.usermgmt.User user;

    @ManyToOne
    @JoinColumn(name = "assigned_to")
    private com.omori.taskmanagement.springboot.model.usermgmt.User assignedTo;

    @ManyToOne
    @JoinColumn(name = "workspace_id")
    private Workspace workspace;

    @ManyToOne
    @JoinColumn(name = "parent_task_id")
    private Task parentTask;

    @Column(nullable = false, name = "sort_order")
    private Integer sortOrder;

    @Column(name = "is_recurring")
    private Boolean isRecurring;

    @Column(columnDefinition = "JSONB")
    private String recurrencePattern;

    @Column(columnDefinition = "JSONB")
    private String metadata;

    @Column(name = "deleted_at")
    private java.sql.Timestamp deletedAt;

    @Column(name = "created_at")
    private java.sql.Timestamp createdAt;

    @Column(name = "updated_at")
    private java.sql.Timestamp updatedAt;

    public enum TaskPriority {
        LOW, MEDIUM, HIGH, URGENT
    }

    public enum TaskStatus {
        PENDING, IN_PROGRESS, COMPLETED, CANCELLED, ON_HOLD
    }
}
