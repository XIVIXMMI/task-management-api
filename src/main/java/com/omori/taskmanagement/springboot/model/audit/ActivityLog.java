package com.omori.taskmanagement.springboot.model.audit;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import com.omori.taskmanagement.springboot.model.project.Task;
import com.omori.taskmanagement.springboot.model.project.Workspace;
import com.omori.taskmanagement.springboot.model.usermgmt.User;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@SuperBuilder
@Entity
@Table(name = "activity_logs", schema = "audit")
public class ActivityLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "task_id")
    private Task task;

    @ManyToOne
    @JoinColumn(name = "workspace_id")
    private Workspace workspace;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ActionType action;

    @Column(nullable = false)
    private String entityType;

    @Column(name = "entity_id")
    private Long entityId;

    @Column(columnDefinition = "JSONB")
    @Convert(converter = JsonbConverter.class)
    private Map<String, Object> oldValues;

    @Column(columnDefinition = "JSONB")
    @Convert(converter = JsonbConverter.class)
    private Map<String, Object> newValues;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent")
    private String userAgent;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
