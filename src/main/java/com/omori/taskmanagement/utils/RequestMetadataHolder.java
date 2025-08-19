package com.omori.taskmanagement.utils;

import com.omori.taskmanagement.dto.common.RequestMetadata;

public class RequestMetadataHolder {
    private static final ThreadLocal<RequestMetadata> CONTEXT =  new ThreadLocal<>();

    public static void setMetadata(RequestMetadata metadata) {
        CONTEXT.set(metadata);
    }

    public static RequestMetadata getMetadata() {
        return CONTEXT.get();
    }

    public static void clear() {
        CONTEXT.remove();
    }
}
