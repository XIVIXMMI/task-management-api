package com.omori.taskmanagement.controller;

import com.omori.taskmanagement.annotations.LogActivity;
import com.omori.taskmanagement.dto.common.ApiResponse;
import com.omori.taskmanagement.dto.project.*;
import com.omori.taskmanagement.model.audit.ActionType;
import com.omori.taskmanagement.model.project.Task;
import com.omori.taskmanagement.security.service.CustomUserDetails;
import com.omori.taskmanagement.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/task")
@Slf4j
@Tag(name = "Task", description = "Task management")
public class TaskController {

    private final TaskService taskService;

    @LogActivity(ActionType.CREATE)
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")      
    @PostMapping("/create")
    @Operation(summary = "Create task", description = "Create new task for user")
    public ResponseEntity<ApiResponse<TaskCreateResponse>> createTask(
            @Valid @RequestBody TaskCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getId();
        Task task = taskService.createTask(userId, request);
        TaskCreateResponse response = TaskCreateResponse.from(task);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @LogActivity(ActionType.VIEW)
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")      
    @GetMapping("/{taskId}")
    @Operation(summary = "Get task by id", description = "Get detail task by id")
    public ResponseEntity<ApiResponse<TaskResponse>> getTaskById(
            @PathVariable Long taskId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getId();
        TaskResponse response = taskService.getTaskById(taskId, userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")      
    @LogActivity(ActionType.VIEW)
    @GetMapping("/uuid/{uuid}")
    @Operation(summary = "Get task by uuid", description = "Get detail task by uuid")
    public ResponseEntity<ApiResponse<TaskResponse>> getTaskByUuid(
            @PathVariable UUID uuid,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getId();
        TaskResponse response = taskService.getTaskByUuid(uuid, userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // Get task with filter and pagination, (sort order by, etc.)
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")      
    @LogActivity(ActionType.VIEW)
    @GetMapping
    @Operation(summary = "Get task with filter", description = "Get user's task with filters and pagination")
    public ResponseEntity<Iterable<TaskResponse>> getTasksByFilter(
            @ModelAttribute TaskFilterRequest filter,
            @AuthenticationPrincipal CustomUserDetails userDetails
            ) {
        Long userId = userDetails.getId();
        Page<TaskResponse> response = taskService.getTasksByUser(userId, filter);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")      
    @LogActivity(ActionType.VIEW)
    @GetMapping("/overdue")
    @Operation( summary = "Get overdue tasks", description = "Get user's overdue tasks")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getOverdueTasks(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getId();
        List<TaskResponse> response = taskService.getOverdueTasks(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")      
    @LogActivity(ActionType.VIEW)
    @GetMapping("/search")
    @Operation( summary = "Search Tasks", description = "Search tasks by keyword ")
    public ResponseEntity<Page<TaskResponse>> searchTasks(
            @RequestParam String keyword,
            @ModelAttribute TaskFilterRequest filter,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getId();
        Page<TaskResponse> response = taskService.searchTasks(userId, keyword, filter);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")      
    @LogActivity(ActionType.UPDATE)
    @PutMapping("/{taskId}")
    @Operation( summary = "Update task", description = "Update user's task detail")
    public ResponseEntity<ApiResponse<TaskResponse>> updateTask(
            @PathVariable Long taskId,
            @Valid @RequestBody TaskUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getId();
        TaskResponse response = taskService.updateTask(taskId, userId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")      
    @LogActivity(ActionType.UPDATE)
    @PatchMapping("/{taskId}/status")
    @Operation( summary = "Update task status", description = "Update user's task status")
    public ResponseEntity<ApiResponse<TaskResponse>> updateTaskStatus(
            @PathVariable Long taskId,
            @RequestParam Task.TaskStatus status,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getId();
        TaskResponse response = taskService.updateTaskStatus(taskId, userId, status);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")      
    @LogActivity(ActionType.UPDATE)
    @PatchMapping("/batch/status")
    @Operation( summary = "Update multiple tasks status", description = "Update status for multiple tasks")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> updateMultipleTasksStatus(
            @RequestBody List<Long> taskId,
            @RequestParam Task.TaskStatus status,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getId();
        List<TaskResponse> response = taskService.updateMultipleTasksStatus(taskId, userId, status);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")      
    @LogActivity(ActionType.UPDATE)
    @PatchMapping("/{taskId}/progress")
    @Operation( summary = "Update task progress", description = "Update user's task progress")
    public ResponseEntity<ApiResponse<TaskResponse>> updateTaskProgress(
            @PathVariable Long taskId,
            @RequestParam Integer progress,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getId();
        TaskResponse response = taskService.updateTaskProgress(taskId, userId, progress);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")      
    @LogActivity(ActionType.DELETE)
    @DeleteMapping("/{taskId}")
    @Operation( summary = "Delete task", description = "Delete user's task")
    public ResponseEntity<ApiResponse<Void>> deleteTask(
            @PathVariable Long taskId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getId();
        taskService.deleteTask(taskId, userId);
        /** Return no content to indicate successful deletion
         * Note: You might want to return a success message or status instead
         * but for RESTful API, no content is a common practice for delete operations.
         * If you want to return a success message, you can modify the ApiResponse accordingly.
         */
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")      
    @LogActivity(ActionType.DELETE)
    @DeleteMapping("/{taskId}/soft")
    @Operation( summary = "Soft delete task", description = "Soft delete user's task")
    public ResponseEntity<ApiResponse<Void>> softDeleteTask(
            @PathVariable Long taskId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getId();
        taskService.softDeleteTask(taskId, userId);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")      
    @LogActivity(ActionType.DELETE)
    @DeleteMapping("/batch")
    @Operation( summary = "Delete multiple tasks", description = "Delete multiple tasks by list of ids")
    public ResponseEntity<ApiResponse<Void>> deleteMultipleTasks(
            @RequestBody List<Long> taskIds,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getId();
        taskService.deleteMultipleTasks(taskIds, userId);
        return ResponseEntity.noContent().build();
    }

}
