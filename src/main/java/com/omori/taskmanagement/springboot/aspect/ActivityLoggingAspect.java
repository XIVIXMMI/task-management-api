package com.omori.taskmanagement.springboot.aspect;

import com.omori.taskmanagement.springboot.model.audit.ActionType;
import com.omori.taskmanagement.springboot.model.audit.ActivityLog;
import com.omori.taskmanagement.springboot.model.usermgmt.User;
import com.omori.taskmanagement.springboot.repository.audit.ActivityLogRepository;
import com.omori.taskmanagement.springboot.security.service.AuthService;
//import com.fasterxml.jackson.core.type.TypeReference;
//import com.fasterxml.jackson.databind.ObjectMapper;
import com.omori.taskmanagement.springboot.annotations.LogActivity;
import com.omori.taskmanagement.springboot.dto.common.RequestMetadata;
import com.omori.taskmanagement.springboot.dto.usermgmt.LoginRequest;
import com.omori.taskmanagement.springboot.dto.usermgmt.RegistrationRequest;
import com.omori.taskmanagement.springboot.service.UserService;

import com.omori.taskmanagement.springboot.utils.RequestMetadataHolder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class ActivityLoggingAspect {

    private final ActivityLogRepository logRepo;
    private final AuthService authService;
    //private final ObjectMapper objectMapper;
    private final UserService userService;

    @Pointcut("@annotation(com.omori.taskmanagement.springboot.annotations.LogActivity)")
    public void loggableMethods() {}

    @Around("loggableMethods()")
    public Object logActivity(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        LogActivity logActivity = method.getAnnotation(LogActivity.class);

        if(logActivity == null) {
            log.warn("No LogActivity annotation found on method: {}", method.getName());
            return joinPoint.proceed();
        }

        ActionType actionType = logActivity.value(); 
        String className = signature.getDeclaringType().getSimpleName();

        RequestMetadata meta = RequestMetadataHolder.getMetadata();
        User currentUser = authService.getCurrentUser();

        if (meta == null) {
            log.warn("Cannot log activity: missing metadata");
            return joinPoint.proceed();
        }

        // Get the original entity for update operations
        // Object originalEntity = null;
        // if (actionType == ActionType.UPDATE) {
        //     Object[] args = joinPoint.getArgs();
        //     if (args != null && args.length > 0) {
        //         originalEntity = getOriginalEntity(args[0]); // Implement this method
        //     }
        // }

        // Proceed with the original method
        Object result = joinPoint.proceed();

        Map<String,Object> newValues = new HashMap<>();
        newValues.put("method", method.getName());
        newValues.put("class",className);

        if(result != null) {
            newValues.put("result",result);
        }

        // Determine the user for logging
        User logUser = currentUser;
        
        // For auth actions, try multiple approaches to get user
        if (logUser == null) {
            if (actionType == ActionType.LOGIN) {
                // For login, try to get user from method arguments (LoginRequest)
                logUser = extractUserFromLoginRequest(joinPoint.getArgs());
            } else if (actionType == ActionType.REGISTER) {
                // For register, the user is created during the method execution
                // We need to extract from request and then find after creation
                logUser = extractUserFromRegisterRequest(joinPoint.getArgs(), result);
            }
        }
        
        // If we still don't have a user, we cannot log
        if (logUser == null) {
            log.warn("Cannot log activity: no user found for action {}", actionType);
            return joinPoint.proceed();
        }

        ActivityLog activityLog = ActivityLog.builder()
            .action(actionType)
            .entityType(className)
            .user(logUser)
            .ipAddress(meta.getIpAddress())
            .userAgent(meta.getUserAgent())
            .createdAt(LocalDateTime.now())
            .newValues(newValues)
            .build();

        try {
            saveActivityLog(activityLog);
        } catch (Exception e) {
            log.error("Error saving activity log: {}", e.getMessage(), e);
        }

        return result;
    }

    @Transactional
    private void saveActivityLog(ActivityLog activityLog) {
        logRepo.save(activityLog);
    }

    private User extractUserFromLoginRequest(Object[] args) {
        try {
            for (Object arg : args) {
                if (arg instanceof LoginRequest) {
                    LoginRequest loginRequest = (LoginRequest) arg;
                    String username = loginRequest.getUsername();
                    log.debug("Extracting user from login request: {}", username);
                    return userService.findByUsername(username);
                }
            }
        } catch (Exception e) {
            log.warn("Failed to extract user from login request", e);
        }
        return null;
    }
    
    private User extractUserFromRegisterRequest(Object[] args, Object result) {
        try {
            for (Object arg : args) {
                if (arg instanceof RegistrationRequest) {
                    RegistrationRequest registerRequest = (RegistrationRequest) arg;
                    String username = registerRequest.getUsername();
                    log.debug("Extracting user from register request: {}", username);
                    // For register, find the user after it's been created
                    return userService.findByUsername(username);
                }
            }
        } catch (Exception e) {
            log.warn("Failed to extract user from register request", e);
        }
        return null;
    }

    // private Object getOriginalEntity(Object id) {
    //     // Implement this method to load the original entity by ID
    //     // For example:
    //     // return repository.findById(id).orElse(null);
    //     return null; // Replace with actual implementation
    // }

    // private Map<String, Object> convertToMap(Object obj) {
    //     if (obj == null) {
    //         return null;
    //     }
    //     return objectMapper.convertValue(obj, new TypeReference<Map<String, Object>>() {});
    // }
}
