package com.omori.taskmanagement.springboot.exceptions;

public class TaskAccessDeniedException  extends RuntimeException{
    public TaskAccessDeniedException(String message){
        super(message);
    }
}
