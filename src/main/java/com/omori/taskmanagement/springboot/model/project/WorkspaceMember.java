package com.omori.taskmanagement.springboot.model.project;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.ColumnDefault;

@Data
@NoArgsConstructor
@SuperBuilder
@Entity
@Table(name = "workspace_members", schema = "project", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"workspace_id", "user_id"})
})
public class WorkspaceMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private com.omori.taskmanagement.springboot.model.usermgmt.User user;

    @Column(nullable = false, name = "role", length = 20)
    @ColumnDefault("'member'")
    private String role;

    @ManyToOne
    @JoinColumn(name = "invited_by")
    private com.omori.taskmanagement.springboot.model.usermgmt.User inviter;

    @Column(nullable = false, name = "joined_at")
    private java.sql.Timestamp joinedAt;
}
