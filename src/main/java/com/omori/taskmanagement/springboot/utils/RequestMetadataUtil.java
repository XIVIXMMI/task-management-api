package com.omori.taskmanagement.springboot.utils;

import org.jboss.logging.MDC;

import com.omori.taskmanagement.springboot.dto.common.RequestMetadata;

import jakarta.servlet.http.HttpServletRequest;

public class RequestMetadataUtil {

    public static RequestMetadata from(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");

        // Try to get the real IP address behind a proxy
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        } else {
            // X-Forwarded-For may contain multiple IPs: client, proxy1, proxy2, etc ...
            ipAddress = ipAddress.split(",")[0].trim();
        }

        // Get the trace ID from MDC, if not present, generate a new one
        String traceId = (String) MDC.get("traceId");
        if (traceId == null) {
            traceId = "no-trace-id"; // Fallback if no trace ID is set
        }

        return new RequestMetadata(
                ipAddress,
                userAgent,
                traceId,
                System.currentTimeMillis(),
                0);
    }
}
