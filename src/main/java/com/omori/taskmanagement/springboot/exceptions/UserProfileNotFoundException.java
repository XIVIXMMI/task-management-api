package com.omori.taskmanagement.springboot.exceptions;

public class UserProfileNotFoundException extends RuntimeException {
    public UserProfileNotFoundException(String message){
        super(message);
    }
}
