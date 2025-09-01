package com.omori.taskmanagement.service.task;

public interface TaskProgressService {

    int calculateTaskProgress(Long taskId);
    void updateProgressFromSubtasks(Long taskId);
    void propagateProgressToParent(Long taskId);
    void updateHierarchyProgress(Long epicId);
}
