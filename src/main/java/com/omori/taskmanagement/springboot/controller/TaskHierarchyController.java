package com.omori.taskmanagement.springboot.controller;

import com.omori.taskmanagement.springboot.annotations.LogActivity;
import com.omori.taskmanagement.springboot.dto.common.ApiResponse;
import com.omori.taskmanagement.springboot.dto.project.HierarchyEpicDto;
import com.omori.taskmanagement.springboot.dto.project.TaskCreateRequest;
import com.omori.taskmanagement.springboot.dto.project.TaskCreateResponse;
import com.omori.taskmanagement.springboot.dto.project.TaskResponse;
import com.omori.taskmanagement.springboot.model.audit.ActionType;
import com.omori.taskmanagement.springboot.model.project.Task;
import com.omori.taskmanagement.springboot.security.service.CustomUserDetails;
import com.omori.taskmanagement.springboot.service.TaskHybridService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/task-hierarchy")
@Tag(name = "Task Hierarchy", description = "Task Hierarchy API")
@Slf4j
@RequiredArgsConstructor
public class TaskHierarchyController {

    private final TaskHybridService taskHybridService;

    @LogActivity(ActionType.CREATE)
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @PostMapping("/epic")
    @Operation(summary = "Create Epic Task", description = "Create new epic task for user")
    public ResponseEntity<ApiResponse<TaskCreateResponse>> createEpicTask(
            @Valid @RequestBody TaskCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Task epicTask = taskHybridService.createEpicTask(
                userDetails.getId(),
                Task.TaskType.EPIC,
                request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(TaskCreateResponse.from(epicTask)));
    }

    @LogActivity(ActionType.CREATE)
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @PostMapping("/story")
    @Operation(summary = "Create Story Task", description = "Create new story task for user")
    public ResponseEntity<ApiResponse<TaskCreateResponse>> createStoryTask(
            @Valid @RequestBody TaskCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Task storyTask = taskHybridService.createStoryTask(
                userDetails.getId(),
                Task.TaskType.STORY,
                request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(TaskCreateResponse.from(storyTask)));
    }

    @LogActivity(ActionType.VIEW)
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @GetMapping("/epic/{taskId}/full")
    @Operation(summary = "View all child tasks", description = "Get all child task for a epic task")
    public ResponseEntity<ApiResponse<HierarchyEpicDto>> getFullHierarchy(
            @PathVariable Long taskId
    ) {
        HierarchyEpicDto hierarchyEpicDto = taskHybridService.getFullHierarchy(taskId);
        return ResponseEntity.ok(ApiResponse.success(hierarchyEpicDto));
    }

    @LogActivity(ActionType.CREATE)
    @PreAuthorize("hasRole('USER' or hasRole('ADMIN')")
    @PostMapping("/task/{taskId}/multiple-subtasks")
    @Operation(summary = "Create multiple subtasks", description = "Create multiple subtask by titles")
    public ResponseEntity<ApiResponse<TaskResponse>> addMultipleSubtasks (
            @PathVariable Long taskId,
            @RequestBody List<String> sustaskTitles
    ) {
        Task task = taskHybridService.addSubtasksToTask(taskId,sustaskTitles);
        return ResponseEntity.ok(ApiResponse.success(TaskResponse.from(task)));
    }


}
