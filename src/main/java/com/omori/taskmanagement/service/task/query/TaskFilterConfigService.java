package com.omori.taskmanagement.service.task.query;

import com.omori.taskmanagement.dto.project.task.TaskFilterRequest;
import org.springframework.data.domain.Pageable;

public interface TaskFilterConfigService {

    TaskFilterRequest resolveFilter(TaskFilterRequest filter);
    TaskFilterRequest createDefaultFilter();
    Pageable createPageable(TaskFilterRequest filter);
}
