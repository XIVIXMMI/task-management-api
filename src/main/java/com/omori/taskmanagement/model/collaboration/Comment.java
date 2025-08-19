package com.omori.taskmanagement.model.collaboration;

import com.omori.taskmanagement.model.project.Task;
import com.omori.taskmanagement.model.usermgmt.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@SuperBuilder
@Entity
@Table(name = "comments", schema = "collaboration")
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "parent_comment_id")
    private Comment parentComment;

    @Column(nullable = false)
    private String content;

    @ElementCollection
    @CollectionTable(
        name = "comment_mentions",
        joinColumns = @JoinColumn(name = "comment_id"),
        schema = "collaboration"
    )
    @Column(name = "user_id")
    private List<Long> mentions;

    @Column(columnDefinition = "JSONB")
    private String attachments;

    @Column(nullable = false, name = "is_edited")
    private Boolean isEdited;

    @Column(name = "edited_at")
    private LocalDateTime editedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
