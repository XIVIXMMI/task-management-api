package com.omori.taskmanagement.springboot.dto.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class RequestMetadata {
    private String userAgent;
    private String ipAddress; 
    private String traceId; // Unique identifier for tracing requests

    private long startTimeMillis; // Timestamp when the request was received
    private long durationMillis; // Duration of the request processing in milliseconds
}
