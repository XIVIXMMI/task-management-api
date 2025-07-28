package com.omori.taskmanagement.springboot.utils;

import com.omori.taskmanagement.springboot.dto.common.RequestMetadata;

import jakarta.servlet.http.HttpServletRequest;

public class RequestMetadataUtil {
    
    public static RequestMetadata from(HttpServletRequest request){
        String userAgent = request.getHeader("User-Agent");

        // Try to get the real IP address behind a proxy
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)){
            ipAddress = request.getRemoteAddr();
        } else {
            // X-Forwarded-For may contain multiple IPs: client, proxy1, proxy2, etc ...
            ipAddress = ipAddress.split(",")[0].trim();
        }

        return new RequestMetadata(ipAddress, userAgent);
    }
}
