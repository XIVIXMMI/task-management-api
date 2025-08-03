package com.omori.taskmanagement.springboot.utils;

import java.io.IOException;

import org.jboss.logging.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.omori.taskmanagement.springboot.dto.common.RequestMetadata;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class RequestTimingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        // Start timing the request
        long startTime = System.currentTimeMillis();
        try {
            RequestMetadata metadata = RequestMetadataHolder.getMetadata();
            if (metadata != null) {
                metadata.setStartTimeMillis(startTime);
            }

            filterChain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - startTime;

            RequestMetadata metadata = RequestMetadataHolder.getMetadata();
            if (metadata != null) {
                metadata.setStartTimeMillis(startTime);
                metadata.setDurationMillis(duration);
            }

            log.info("Request processed in {} ms", duration);
        }
    }
}
