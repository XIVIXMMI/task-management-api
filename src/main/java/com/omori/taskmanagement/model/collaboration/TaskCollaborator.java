package com.omori.taskmanagement.model.collaboration;

import com.omori.taskmanagement.model.project.Task;
import com.omori.taskmanagement.model.usermgmt.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import java.time.LocalDateTime;
import org.hibernate.annotations.ColumnDefault;

@Data
@NoArgsConstructor
@SuperBuilder
@Entity
@Table(name = "task_collaborators", schema = "collaboration", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"task_id", "user_id"})
})
public class TaskCollaborator {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, name = "role", length = 20)
    @ColumnDefault("'viewer'")
    private String role;

    @ManyToOne
    @JoinColumn(name = "invited_by")
    private User inviter;

    @Column(nullable = false, name = "invited_at")
    private LocalDateTime invitedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
