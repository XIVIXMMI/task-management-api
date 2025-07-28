package com.omori.taskmanagement.springboot.aspect;

import com.omori.taskmanagement.springboot.model.audit.ActionType;
import com.omori.taskmanagement.springboot.model.audit.ActivityLog;
import com.omori.taskmanagement.springboot.model.usermgmt.User;
import com.omori.taskmanagement.springboot.repository.audit.ActivityLogRepository;
import com.omori.taskmanagement.springboot.security.service.AuthService;
import com.omori.taskmanagement.springboot.dto.common.RequestMetadata;

import com.omori.taskmanagement.springboot.utils.RequestMetadataHolder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class ActivityLoggingAspect {

    private final ActivityLogRepository logRepo;
    private final AuthService authService;

    @Pointcut("execution(* com.omori.taskmanagement.springboot.service.*.*(..))")
    public void serviceMethods() {}

    @AfterReturning(pointcut = "serviceMethods()", returning = "result")
    public void logActivity(JoinPoint joinPoint, Object result) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String method = signature.getMethod().getName();
        String className = signature.getDeclaringType().getSimpleName();

        RequestMetadata meta = RequestMetadataHolder.getMetadata();
        User currentUser = authService.getCurrentUser();

        System.out.println("Current User: " + currentUser);

        if (meta == null || currentUser == null) {
            log.warn("Cannot log activity: missing metadata or user");
            return;
        }

        ActivityLog activityLog = ActivityLog.builder()
            .action(ActionType.fromMethod(method))
            .entityType(className)
            .user(currentUser)
            .ipAddress(meta.getIpAddress())
            .userAgent(meta.getUserAgent())
            .createdAt(LocalDateTime.now())
            .build();

        try {
            logRepo.save(activityLog);
        } catch (Exception e) {
            log.error("Error saving activity log: {}", e.getMessage(), e);
        }
    }
}
