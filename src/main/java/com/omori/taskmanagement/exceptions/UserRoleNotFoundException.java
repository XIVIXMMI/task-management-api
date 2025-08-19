package com.omori.taskmanagement.exceptions;


public class UserRoleNotFoundException extends RuntimeException {
    public UserRoleNotFoundException(String message){
        super(message);
    }
}
