package com.omori.taskmanagement.springboot.model.project;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.omori.taskmanagement.springboot.model.audit.JsonbConverter;
import com.omori.taskmanagement.springboot.model.usermgmt.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

import java.util.*;

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
    private UUID uuid;

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
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, name = "priority")
    private TaskPriority priority;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, name = "status")
    private TaskStatus status;

    @Column(name = "estimated_hours")
    private Double estimatedHours;

    @Column(name = "actual_hours")
    private Double actualHours;

    @Column(nullable = false, name = "progress")
    private Integer progress;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = true)
    private Category category;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
    @JoinColumn(name = "assigned_to", nullable = true)
    private User assignedTo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = true)
    private Workspace workspace;

    @ManyToOne
    @JoinColumn(name = "parent_task_id")
    private Task parentTask;

    @Column(name = "task_type", nullable = false)
    @Enumerated(EnumType.STRING)
    @NotNull
    private TaskType taskType; // EPIC, STORY, TASK

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Builder.Default
    private List<Subtask> subtasks = new ArrayList<>();

    @Column(nullable = false, name = "sort_order")
    private Integer sortOrder;

    @Column(name = "is_recurring")
    private Boolean isRecurring;

    @Column(columnDefinition = "JSONB")
    @JdbcTypeCode(SqlTypes.JSON)
    @Convert(converter = JsonbConverter.class)
    private Map<String, Object> recurrencePattern;

    @Column(columnDefinition = "JSONB")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> metadata;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum TaskPriority {
        low, medium, high, urgent
    }

    public enum TaskStatus {
        pending, in_progress, completed, cancelled, on_hold
    }

    @Getter
    public enum TaskType {
        EPIC(0), STORY(1), TASK(2);

        private final int level;

        TaskType(int level) {
            this.level = level;
        }
        // Helper methods for level comparison
        public boolean isHigherThan(TaskType other) {
            return this.level < other.level; // Lower level = higher priority
        }

        public boolean isLowerThan(TaskType other) {
            return this.level > other.level;
        }

        public boolean canContain(TaskType childType) {
            return this.level < childType.level;
        }

        // Convert level to TaskType
        public static TaskType fromLevel(int level) {
            return Arrays.stream(values())
                    .filter(type -> type.level == level)
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Invalid level: " + level));
        }
    }

    @PrePersist
    protected void onCreate() {
        if (uuid == null) {
            uuid = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
        if (priority == null) {
            priority = TaskPriority.medium;
        }
        if (status == null) {
            status = TaskStatus.pending;
        }
        if (progress == null) {
            progress = 0;
        }
        if (sortOrder == null) {
            sortOrder = 0;
        }
        if (isRecurring == null) {
            isRecurring = false;
        }

        if (taskType == null) {
            taskType = TaskType.TASK; // Default is TASK
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

}
