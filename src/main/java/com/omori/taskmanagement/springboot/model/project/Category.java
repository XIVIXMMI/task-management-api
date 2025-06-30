package com.omori.taskmanagement.springboot.model.project;

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
@Table(name = "categories", schema = "project")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false, name = "color", length = 7)
    @ColumnDefault("'#3498db'")
    private String color;

    @Column(name = "icon")
    private String icon;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private com.omori.taskmanagement.springboot.model.usermgmt.User user;

    @ManyToOne
    @JoinColumn(name = "workspace_id")
    private Workspace workspace;

    @Column(name = "is_default")
    private Boolean isDefault;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(nullable = false, name = "sort_order")
    private Integer sortOrder;

}
