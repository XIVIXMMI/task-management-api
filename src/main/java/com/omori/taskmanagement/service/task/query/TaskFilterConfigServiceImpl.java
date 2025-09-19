package com.omori.taskmanagement.service.task.query;

import com.omori.taskmanagement.dto.project.task.TaskFilterRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Component
public class TaskFilterConfigServiceImpl implements TaskFilterConfigService{

    @Value("${task.default.page-size:10}")
    private int defaultPageSize;

    @Value("${task.default.sort-by:createdAt}")
    private String defaultSortBy;

    @Override
    public TaskFilterRequest resolveFilter(TaskFilterRequest filter) {
        return Optional.ofNullable(filter)
                .orElseGet(this::createDefaultFilter);    }

    @Override
    public TaskFilterRequest createDefaultFilter() {
        return TaskFilterRequest.builder()
                .page(0)
                .size(defaultPageSize)
                .sortBy(defaultSortBy)
                .sortDirection("DESC")
                .build();
    }

    @Override
    public Pageable createPageable(TaskFilterRequest filter) {
        TaskFilterRequest effectiveFilter = resolveFilter(filter);
        Sort.Direction direction = "DESC".equalsIgnoreCase(effectiveFilter.getSortDirection())
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        Sort sort = Sort.by(direction, effectiveFilter.getSortBy());
        return PageRequest.of(
                effectiveFilter.getPage(),
                effectiveFilter.getSize(),
                sort
        );    }
}
