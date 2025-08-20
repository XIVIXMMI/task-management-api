package com.omori.taskmanagement.repository.audit;

import com.omori.taskmanagement.model.audit.ErrorLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import jakarta.annotation.Nonnull;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ErrorLogRepository extends JpaRepository<ErrorLog, Long> {
    Optional<ErrorLog> findById(@Nonnull Long id);
    
    List<ErrorLog> findByUserId(@Nonnull Long userId);
    
    List<ErrorLog> findByErrorType(@Nonnull String errorType);
    
    List<ErrorLog> findByCreatedAtBetween(@Nonnull LocalDateTime start, @Nonnull LocalDateTime end);
}
