package com.omori.taskmanagement.springboot.exceptions.task;

public class WorkspaceNotFoundException extends RuntimeException{
    public WorkspaceNotFoundException(String message){
        super(message);
    }
}
