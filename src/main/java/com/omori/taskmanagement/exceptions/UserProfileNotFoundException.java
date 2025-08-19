package com.omori.taskmanagement.exceptions;

public class UserProfileNotFoundException extends RuntimeException {
    public UserProfileNotFoundException(String message){
        super(message);
    }
}
