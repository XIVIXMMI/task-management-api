package com.omori.taskmanagement.springboot.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.omori.taskmanagement.springboot.model.audit.ActionType;

/**
 * Annotation to mark a method for logging activity
 * 
 * @author omori
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LogActivity {
    ActionType value();
}
