package com.omori.taskmanagement.model.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Event published when a task's progress needs to be updated based on subtask changes.
 * This helps break circular dependencies between SubTaskService and TaskHybridService.
 */
@Getter
public class TaskProgressUpdateEvent extends ApplicationEvent {
    
    private final Long taskId;
    private final String reason;
    
    public TaskProgressUpdateEvent(Object source, Long taskId, String reason) {
        super(source);
        this.taskId = taskId;
        this.reason = reason;
    }
}