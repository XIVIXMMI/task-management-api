package com.omori.taskmanagement.springboot.utils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class ObjectDiffUtils {

    public static Map<String, Object> getObjectAsMap(Object obj) {
        Map<String, Object> map = new HashMap<>();
        if(obj == null) {
            return map;
        }

        try {
            for(Field field : obj.getClass().getDeclaredFields()){
                field.setAccessible(true);
                Object value = field.get(obj);
                if(!field.getName().equals("password") &&
                !field.getName().equals("createdAt") &&
                !field.getName().equals("updatedAt") &&
                !field.getName().equals("deletedAt")) {
                    map.put(field.getName(), value);
                }
            }
        } catch(IllegalAccessException e) {
            e.printStackTrace();
        }
        return map;
    }

    public static Map<String, Object> getChangeFields(Object oldObj, Object newObj) {
        Map<String,Object> oldMap = getObjectAsMap(oldObj);
        Map<String,Object> newMap = getObjectAsMap(newObj);    
        Map<String,Object> changes = new HashMap<>();

        for (Map.Entry<String, Object> entry : newMap.entrySet()) {
            String key = entry.getKey();
            Object newValue = entry.getValue();
            Object oldValue = oldMap.get(key);

            if((oldValue == null && newValue != null) || 
                (oldValue !=null && !oldValue.equals(newValue))){
                changes.put(key, Map.of(
                    "old", oldValue,
                    "new", newValue
                ));
            }
        }
        return changes;
    }
    
}
