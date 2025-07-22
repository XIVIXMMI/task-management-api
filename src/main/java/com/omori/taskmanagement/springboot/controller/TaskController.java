package com.omori.taskmanagement.springboot.controller;

import com.omori.taskmanagement.springboot.dto.project.*;
import com.omori.taskmanagement.springboot.model.project.Task;
import com.omori.taskmanagement.springboot.repository.usermgmt.UserRepository;
import com.omori.taskmanagement.springboot.security.service.CustomUserDetails;
import com.omori.taskmanagement.springboot.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
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
@Tag(name = "Task", description = "Task management API")
public class TaskController {

    private final TaskService taskService;
    private final UserRepository userRepository;

    @PostMapping("/create")
    @PreAuthorize("@authService.hasPermisson(#username")
    @Operation(summary = "Create task", description = "Create new task for user")
    public ResponseEntity<CreateTaskResponse> createTask(
            @Valid @RequestBody CreateTaskRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getId();
        Task task = taskService.createTask(userId, request);
        CreateTaskResponse response = CreateTaskResponse.from(task);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{taskId}")
    @Operation(summary = "Get task by id", description = "Get detail task by id")
    public ResponseEntity<GetTaskResponse> getTaskById(
            @PathVariable Long taskId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getId();
        GetTaskResponse response = taskService.getTaskById(taskId, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/uuid/{uuid}")
    @Operation(summary = "Get task by uuid", description = "Get detail task by uuid")
    public ResponseEntity<GetTaskResponse> getTaskByUuid(
            @PathVariable UUID uuid,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getId();
        GetTaskResponse response = taskService.getTaskByUuid(uuid, userId);
        return ResponseEntity.ok(response);
    }

    // READ - Get list with filter
    @GetMapping
    @Operation(summary = "Get task", description = "Get user's task with filters and pagination")
    public ResponseEntity<Iterable<GetTaskResponse>> getTasksByFilter(
            @ModelAttribute TaskFilterRequest filter,
            @AuthenticationPrincipal CustomUserDetails userDetails
            ) {
        Long userId = userDetails.getId();
        Page<GetTaskResponse> response = taskService.getTasksByUser(userId, filter);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/overdue")
    @Operation( summary = "Get overdue tasks", description = "Get user's overdue tasks")
    public ResponseEntity<List<GetTaskResponse>> getOverdueTasks(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getId();
        List<GetTaskResponse> response = taskService.getOverdueTasks(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    @Operation( summary = "Search Tasks", description = "Search tasks by keyword ")
    public ResponseEntity<Page<GetTaskResponse>> searchTasks(
            @RequestParam String keyword,
            @ModelAttribute TaskFilterRequest filter,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getId();
        Page<GetTaskResponse> response = taskService.searchTasks(userId, keyword, filter);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{taskId}")
    @Operation( summary = "Update task", description = "Update user's task detail")
    public ResponseEntity<GetTaskResponse> updateTask(
            @PathVariable Long taskId,
            @Valid @RequestBody UpdateTaskRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getId();
        GetTaskResponse response = taskService.updateTask(taskId, userId, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{taskId}/status")
    @Operation( summary = "Update task status", description = "Update user's task status")
    public ResponseEntity<GetTaskResponse> updateTaskStatus(
            @PathVariable Long taskId,
            @RequestParam Task.TaskStatus status,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getId();
        GetTaskResponse response = taskService.updateTaskStatus(taskId, userId, status);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/batch/status")
    @Operation( summary = "Update multiple tasks status", description = "Update status for")
    public ResponseEntity<List<GetTaskResponse>> updateMultipleTasksStatus(
            @RequestBody List<Long> taskId,
            @RequestParam Task.TaskStatus status,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getId();
        List<GetTaskResponse> response = taskService.updateMultipleTasksStatus(taskId, userId, status);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{taskId}/progress")
    @Operation( summary = "Update task progress", description = "Update user's task progress")
    public ResponseEntity<GetTaskResponse> updateTaskProgress(
            @PathVariable Long taskId,
            @RequestParam Integer progress,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getId();
        GetTaskResponse response = taskService.updateTaskProgress(taskId, userId, progress);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{taskId}")
    @Operation( summary = "Delete task", description = "Delete user's task")
    public ResponseEntity<Void> deleteTask(
            @PathVariable Long taskId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getId();
        taskService.deleteTask(taskId, userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{taskId}/soft")
    @Operation( summary = "Soft delete task", description = "Soft delete user's task")
    public ResponseEntity<Void> softDeleteTask(
            @PathVariable Long taskId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getId();
        taskService.softDeleteTask(taskId, userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/batch")
    @Operation( summary = "Delete multiple tasks", description = "Delete multiple tasks by list of ids")
    public ResponseEntity<Void> deleteMultipleTasks(
            @RequestBody List<Long> taskIds,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getId();
        taskService.deleteMultipleTasks(taskIds, userId);
        return ResponseEntity.noContent().build();
    }


}
