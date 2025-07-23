package com.omori.taskmanagement.springboot.exceptions;

public class WorkspaceNotFoundException extends RuntimeException{
    public WorkspaceNotFoundException(String message){
        super(message);
    }
}
