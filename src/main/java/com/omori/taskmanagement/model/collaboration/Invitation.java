package com.omori.taskmanagement.model.collaboration;

import com.omori.taskmanagement.model.project.Workspace;
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
@Table(name = "invitations", schema = "collaboration")
public class Invitation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "invitation_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    @ManyToOne
    @JoinColumn(name = "inviter_id", nullable = false)
    private User inviter;

    @Column(nullable = false, name = "invitee_email")
    private String inviteeEmail;

    @Column(nullable = false, name = "role", length = 20)
    @ColumnDefault("'member'")
    private String role;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false, name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;

    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
