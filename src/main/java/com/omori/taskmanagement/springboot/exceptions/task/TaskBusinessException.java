package com.omori.taskmanagement.springboot.exceptions.task;

public class TaskBusinessException extends RuntimeException {
    public TaskBusinessException(String message) {
        super(message);
    }
    
    // throw custom exception và giữ nguyên nhân gốc (stack trace)
    public TaskBusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
