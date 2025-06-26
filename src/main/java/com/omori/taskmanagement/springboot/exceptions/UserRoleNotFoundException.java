package com.omori.taskmanagement.springboot.exceptions;


public class UserRoleNotFoundException extends RuntimeException {
    public UserRoleNotFoundException(String message){
        super(message);
    }
}
