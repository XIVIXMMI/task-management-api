package com.omori.taskmanagement.springboot.model.project;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import java.time.LocalDateTime;
import org.hibernate.annotations.ColumnDefault;

import com.omori.taskmanagement.springboot.model.usermgmt.User;

@Data
@NoArgsConstructor
@SuperBuilder
@Entity
@Table(name = "tags", schema = "project", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"name", "user_id", "workspace_id"})
})
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tag_id")
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, name = "color", length = 7)
    @ColumnDefault("'#95a5a6'")
    private String color;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "workspace_id")
    private Workspace workspace;

    @Column(nullable = false, name = "usage_count")
    private Integer usageCount;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
