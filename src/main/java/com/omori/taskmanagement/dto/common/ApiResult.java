package com.omori.taskmanagement.dto.common;


import java.time.LocalDateTime;

import com.omori.taskmanagement.utils.RequestMetadataHolder;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Generic API response wrapper")
public class ApiResult<T> {
    private boolean success;
    private String message;
    private T data;
    private LocalDateTime timestamp;
    private String traceId;

    public static <T> ApiResult<T> success(T data, String message) {
        RequestMetadata metadata = RequestMetadataHolder.getMetadata();
        return ApiResult.<T>builder()
            .success(true)
            .message(message)
            .data(data)
            .timestamp(LocalDateTime.now())
            .traceId(metadata != null ? metadata.getTraceId() : null)
            .build();
    }

    public static <T> ApiResult<T> success(T data) {
        return success(data, "Operation completed successfully");
    }

    public static <T> ApiResult<T> error (String message) {
        return ApiResult.<T>builder()
            .success(false)
            .message(message)
            .data(null)
            .timestamp(LocalDateTime.now())
            .build();
    }

    public static <T> ApiResult<T> error (String message, String traceId) {
        return ApiResult.<T>builder()
            .success(false)
            .message(message)
            .data(null)
            .timestamp(LocalDateTime.now())
            .traceId(traceId)
            .build();
    }
}

