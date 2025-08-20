package com.omori.taskmanagement.exceptions.task;

public class TaskAccessDeniedException  extends RuntimeException{
    public TaskAccessDeniedException(String message){
        super(message);
    }
}
