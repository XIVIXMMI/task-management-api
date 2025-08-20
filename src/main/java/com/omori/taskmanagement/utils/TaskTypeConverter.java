package com.omori.taskmanagement.utils;

import com.omori.taskmanagement.model.project.Task;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Converter(autoApply = false)
public class TaskTypeConverter implements AttributeConverter<Task.TaskType, String> {

    @Override
    public String convertToDatabaseColumn(Task.TaskType attribute) {
        if( attribute == null) return Task.TaskType.TASK.name();
        return attribute.name();
    }

    @Override
    public Task.TaskType convertToEntityAttribute(String dbData) {
        if ( dbData == null || dbData.trim().isEmpty()){
            return Task.TaskType.TASK;
        }
        try {
            return Task.TaskType.valueOf(dbData);
        } catch ( IllegalArgumentException e) {
            log.warn("Invalid task type value in database: {}, using default task", dbData);
            return Task.TaskType.TASK;
        }
    }
}
