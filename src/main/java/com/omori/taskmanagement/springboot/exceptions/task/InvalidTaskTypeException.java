package com.omori.taskmanagement.springboot.exceptions.task;

public class InvalidTaskTypeException extends RuntimeException{
    public InvalidTaskTypeException(String message){
        super(message);
    }
}
