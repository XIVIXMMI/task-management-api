package com.omori.taskmanagement.springboot.utils;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import com.omori.taskmanagement.springboot.dto.common.RequestMetadata;

import lombok.Getter;
import lombok.Setter;

@Component
@RequestScope
@Getter
@Setter
public class RequestMetadataContext {
    private RequestMetadata metadata;   
}
