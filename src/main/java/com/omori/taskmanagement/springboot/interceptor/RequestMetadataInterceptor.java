package com.omori.taskmanagement.springboot.interceptor;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.omori.taskmanagement.springboot.dto.common.RequestMetadata;
import com.omori.taskmanagement.springboot.utils.RequestMetadataUtil;
import com.omori.taskmanagement.springboot.utils.RequestMetadataHolder;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RequestMetadataInterceptor implements HandlerInterceptor{
    /**
     * Intercept the request to extract metadata and store it in the context
     * @param request The incoming HTTP request
     * @param response The HTTP response
     * @param handler The handler for the request
     * @return true to continue processing, false to stop
     */
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Extract metadata from the request
        RequestMetadata metadata = RequestMetadataUtil.from(request);
        
        // Store the metadata in the context
        RequestMetadataHolder.setMetadata(metadata);

        // Continue with the request processing
        return true;
    }
    
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // Clear the metadata after the request is completed avoid memory leaks
        RequestMetadataHolder.clear();
    }
}
