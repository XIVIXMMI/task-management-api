package com.omori.taskmanagement.exceptions.task;

public class WorkspaceNotFoundException extends RuntimeException{
    public WorkspaceNotFoundException(String message){
        super(message);
    }
}
