package com.omori.taskmanagement.controller;

import com.omori.taskmanagement.annotations.LogActivity;
import com.omori.taskmanagement.dto.common.ApiResponse;
import com.omori.taskmanagement.dto.project.subtask.SubtaskResponse;
import com.omori.taskmanagement.dto.project.task.HierarchyEpicDto;
import com.omori.taskmanagement.dto.project.task.TaskCreateRequest;
import com.omori.taskmanagement.dto.project.task.TaskCreateResponse;
import com.omori.taskmanagement.model.audit.ActionType;
import com.omori.taskmanagement.model.project.Subtask;
import com.omori.taskmanagement.model.project.Task;
import com.omori.taskmanagement.security.service.CustomUserDetails;
import com.omori.taskmanagement.service.task.TaskHybridService;
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
import java.util.stream.Collectors;

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
    @Operation(summary = "Create Standalone Story Task", description = "Create new story task for user")
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

    // Create STORY under EPIC
    @LogActivity(ActionType.CREATE)
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @PostMapping("/epic/{epicId}/story")
    @Operation(summary = "Create Story under Epic", description = "Create new story for user")
    public ResponseEntity<ApiResponse<TaskCreateResponse>> createStoryUnderEpic(
            @PathVariable Long epicId,
            @Valid @RequestBody TaskCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        request.setParentId(epicId);
        request.setType(Task.TaskType.STORY);

        Task task = taskHybridService.createStoryTask(
                userDetails.getId(),
                Task.TaskType.STORY,
                request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(TaskCreateResponse.from(task)));
    }

    // Create TASK under STORY
    @LogActivity(ActionType.CREATE)
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @PostMapping("/story/{storyId}/task")
    @Operation(summary = "Create Task under Story", description = "Create new task for user")
    public ResponseEntity<ApiResponse<TaskCreateResponse>> createTask(
            @PathVariable Long storyId,
            @Valid @RequestBody TaskCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        request.setParentId(storyId);
        request.setType(Task.TaskType.TASK);

        Task task = taskHybridService.createTask(
                userDetails.getId(),
                Task.TaskType.TASK,
                request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(TaskCreateResponse.from(task)));
    }

    @LogActivity(ActionType.VIEW)
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @GetMapping("/epic/{taskId}/hierarchy")
    @Operation(summary = "View all child tasks", description = "Get all child task for an epic task")
    public ResponseEntity<ApiResponse<HierarchyEpicDto>> getFullHierarchy(
            @PathVariable Long taskId
    ) {
        HierarchyEpicDto hierarchyEpicDto = taskHybridService.getFullHierarchy(taskId);
        return ResponseEntity.ok(ApiResponse.success(hierarchyEpicDto));
    }

    @LogActivity(ActionType.VIEW)
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @GetMapping("/epic/uuid/{epicUuid}/hierarchy")
    @Operation(summary = "View all child task by Uuid", description = "Get all child task for an epic task by uuid")
    public ResponseEntity<ApiResponse<HierarchyEpicDto>> getFullHierarchyByUuid(
            @PathVariable String epicUuid
    ) {
        HierarchyEpicDto hierarchyEpicDto = taskHybridService.getFullHierarchyByUuid(epicUuid);
        return ResponseEntity.ok(ApiResponse.success(hierarchyEpicDto));
    }

    @LogActivity(ActionType.CREATE)
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @PostMapping("/task/{taskId}/multiple-subtasks")
    @Operation(summary = "Create multiple subtasks", description = "Create multiple subtasks by titles")
    public ResponseEntity<ApiResponse<List<SubtaskResponse>>> addMultipleSubtasks (
            @PathVariable Long taskId,
            @RequestBody List<String> subtaskTitles
    ) {
        List<Subtask> subtasks = taskHybridService.addSubtasksToTask(taskId,subtaskTitles);
        List<SubtaskResponse> response = subtasks.stream()
                .map(SubtaskResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(response));
    }



}
