package com.omori.taskmanagement.springboot.exceptions;

import java.util.Map;

import lombok.Getter;

@Getter
public class TaskValidationException extends RuntimeException {

    // map <field name, error message>
    private final Map<String, String> validationErrors;
    
    public TaskValidationException(String message, Map<String, String> validationErrors) {
        super(message);
        this.validationErrors = validationErrors;
    }

    public TaskValidationException(String message) {
        super(message);
        this.validationErrors = Map.of();
    }
}
