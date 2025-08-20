package com.omori.taskmanagement.exceptions.task;

public class InvalidTaskTypeException extends RuntimeException{
    public InvalidTaskTypeException(String message){
        super(message);
    }
}
