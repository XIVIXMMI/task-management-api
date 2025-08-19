package com.omori.taskmanagement.model.collaboration;

import com.omori.taskmanagement.model.project.Task;
import com.omori.taskmanagement.model.usermgmt.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@SuperBuilder
@Entity
@Table(name = "attachments", schema = "collaboration")
public class Attachment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attachment_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "task_id")
    private Task task;

    @ManyToOne
    @JoinColumn(name = "comment_id")
    private Comment comment;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String filename;

    @Column(nullable = false, name = "original_filename")
    private String originalFilename;

    @Column(nullable = false, name = "file_path")
    private String filePath;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(nullable = false, name = "file_size")
    private Long fileSize;

    @Column(nullable = false, name = "mime_type")
    private String mimeType;

}
