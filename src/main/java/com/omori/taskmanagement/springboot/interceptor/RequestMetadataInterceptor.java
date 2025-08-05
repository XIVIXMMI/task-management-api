package com.omori.taskmanagement.springboot.interceptor;

import java.util.UUID;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.omori.taskmanagement.springboot.dto.common.RequestMetadata;
import com.omori.taskmanagement.springboot.utils.RequestMetadataUtil;
import com.omori.taskmanagement.springboot.utils.RequestMetadataHolder;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class RequestMetadataInterceptor implements HandlerInterceptor {
    /**
     * Intercept the request to extract metadata and store it in the context
     * 
     * @param request  The incoming HTTP request
     * @param response The HTTP response
     * @param handler  The handler for the request
     * @return true to continue processing, false to stop
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        // Genarate or extract trace ID from the request
        String traceId = UUID.randomUUID().toString();

        // Put trace ID into SLF4J MDC for logging
        MDC.put("traceId", traceId);

        // Extract metadata from the request
        RequestMetadata metadata = RequestMetadataUtil.from(request);
        metadata.setTraceId(traceId); // Ensure your RequestMetadata class has a traceId field

        // Store the metadata in the context
        RequestMetadataHolder.setMetadata(metadata);

        // Continue with the request processing
        return true;
    }

    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
            throws Exception {
        // Retrieve the metadata from the context
        RequestMetadata metadata = RequestMetadataHolder.getMetadata();
        if (metadata != null) {
            log.info("Request completed for traceId={}", metadata.getTraceId());
        }
        // Clear the metadata after the request is completed avoid memory leaks
        RequestMetadataHolder.clear();

    MDC.remove("traceId"); // Clear the MDC to avoid memory leaks
    }
}
