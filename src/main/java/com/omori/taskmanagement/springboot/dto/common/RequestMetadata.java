package com.omori.taskmanagement.springboot.dto.common;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RequestMetadata {
    private String userAgent;
    private String ipAddress; 
}
