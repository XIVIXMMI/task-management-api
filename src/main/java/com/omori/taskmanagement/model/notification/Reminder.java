package com.omori.taskmanagement.model.notification;

import com.omori.taskmanagement.model.project.Task;
import com.omori.taskmanagement.model.usermgmt.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@SuperBuilder
@Entity
@Table(name = "reminders", schema = "notification")
public class Reminder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reminder_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, name = "remind_at")
    private LocalDateTime remindAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "repeat_interval", columnDefinition = "repeat_interval DEFAULT 'none'")
    private RepeatInterval repeatInterval;

    @ElementCollection
    @CollectionTable(
        name = "reminder_notification_types",
        joinColumns = @JoinColumn(name = "reminder_id"),
        schema = "notification"
    )
    @Column(name = "notification_type")
    private Set<NotificationType> notificationTypes;

    @Column(nullable = false, name = "is_sent")
    private Boolean isSent;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(nullable = false, name = "is_active")
    private Boolean isActive;

    @Column(columnDefinition = "JSONB")
    private String metadata;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum RepeatInterval {
        NONE, DAILY, WEEKLY, MONTHLY, YEARLY
    }

    public enum NotificationType {
        EMAIL, PUSH, SMS
    }
}
