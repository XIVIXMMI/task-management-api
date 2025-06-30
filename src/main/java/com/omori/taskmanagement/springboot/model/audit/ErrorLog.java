package com.omori.taskmanagement.springboot.model.audit;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import com.omori.taskmanagement.springboot.model.usermgmt.User;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@SuperBuilder
@Entity
@Table(name = "error_logs", schema = "audit")
public class ErrorLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "error_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "error_type", nullable = false)
    private String errorType;

    @Column(name = "error_message", nullable = false)
    private String errorMessage;

    @Column(name = "stack_trace")
    private String stackTrace;

    @Column(columnDefinition = "JSONB")
    @Convert(converter = JsonbConverter.class)
    private Map<String, Object> requestData;

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
