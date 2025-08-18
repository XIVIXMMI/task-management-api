package com.omori.taskmanagement.springboot.exceptions.task;

public class TaskAccessDeniedException  extends RuntimeException{
    public TaskAccessDeniedException(String message){
        super(message);
    }
}
