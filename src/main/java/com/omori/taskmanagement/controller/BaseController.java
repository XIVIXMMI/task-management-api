package com.omori.taskmanagement.controller;

import com.omori.taskmanagement.dto.common.ApiResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.function.Supplier;

@Slf4j
public abstract class BaseController {

    protected  <T> ResponseEntity<ApiResult<T>> ok(T body) {
        return ResponseEntity.ok(ApiResult.success(body));
    }

    protected <T> ResponseEntity<ApiResult<T>> created(T body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResult.success(body));
    }

    protected void logTaskRequest(Long userId, String operation) {
        log.debug("User {} requesting tasks via {}", userId, operation);
    }

    protected void logTaskSuccess(Long userId, String operation) {
        log.debug("User {} successfully requested tasks via {}", userId, operation);
    }

    protected void logTaskRequest(Long userId, String operation, Object... params) {
        log.debug("User {} requesting {} with params: {}", userId, operation, Arrays.toString(params));
    }

    protected void logOperationStart(Long userId, String operation, Object... params) {
        if (params.length > 0) {
            log.debug("User {} starting {} with params: {}", userId, operation, Arrays.toString(params));
        } else {
            log.debug("User {} starting {}", userId, operation);
        }
    }

    protected void logOperationSuccess(Long userId, String operation) {
        log.debug("User {} successfully completed {}", userId, operation);
    }

    protected void logOperationError(Long userId, String operation, Exception e) {
        log.error("User {} failed to complete {}: {}", userId, operation, e.getMessage());
    }

    // Method name extraction utility
    protected String getCurrentMethodName() {
        return Thread.currentThread().getStackTrace()[2].getMethodName();
    }

    // Wrapper method for common task query pattern
    protected <T> ResponseEntity<ApiResult<T>> executeTaskQuery(
            Long userId,
            String operation,
            Supplier<T> querySupplier) {

        logTaskRequest(userId, operation);
        try {
            T result = querySupplier.get();
            logTaskSuccess(userId, operation);
            return ok(result);
        } catch (Exception e) {
            logOperationError(userId, operation, e);
            throw e;
        }
    }
}
