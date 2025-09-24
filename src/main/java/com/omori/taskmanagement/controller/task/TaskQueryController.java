package com.omori.taskmanagement.controller.task;

import com.omori.taskmanagement.annotations.LogActivity;
import com.omori.taskmanagement.dto.common.ApiResult;
import com.omori.taskmanagement.dto.project.task.TaskFilterRequest;
import com.omori.taskmanagement.dto.project.task.TaskResponse;
import com.omori.taskmanagement.model.audit.ActionType;
import com.omori.taskmanagement.model.project.Task;
import com.omori.taskmanagement.security.service.CustomUserDetails;
import com.omori.taskmanagement.service.task.query.TaskQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/tasks")
@Slf4j
@Tag(name = "Task Management")
public class TaskQueryController {

    private final TaskQueryService taskQueryService;

    @LogActivity(ActionType.VIEW)
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @GetMapping("/{taskId}")
    @Operation(
            summary = "Get a task by ID",
            description = "Retrieve a task by its ID with full details including relationships"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Task found"),
            @ApiResponse(responseCode = "400",
                    description = "Invalid task ID provided"),
            @ApiResponse(responseCode = "404",
                    description = "Task not found"),
            @ApiResponse(responseCode = "403",
                    description = "Forbidden - not allowed to view this task")
    })
    public ResponseEntity<ApiResult<TaskResponse>> getTaskById(
            @Parameter(description = "ID of the task to retrieve",
                    example = "17",
                    required = true)
            @PathVariable @Positive Long taskId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        log.debug("User {} requesting task with ID: {}", userDetails.getId(), taskId);
        TaskResponse taskResponse = taskQueryService.getTaskById(taskId, userDetails.getId());
        log.debug("Successfully retrieved task {} for user {}", taskId, userDetails.getId());
        return ResponseEntity.ok(ApiResult.success(taskResponse));
    }

    @LogActivity(ActionType.VIEW)
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @GetMapping("/uuid/{uuid}")
    @Operation(
            summary = "Get a task by UUID",
            description = "Retrieve a task by its UUID with full details including relationships"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Task found"),
            @ApiResponse(responseCode = "400",
                    description = "Invalid task UUID provided"),
            @ApiResponse(responseCode = "404",
                    description = "Task not found"),
            @ApiResponse(responseCode = "403",
                    description = "Forbidden - not allowed to view this task")
    })
    public ResponseEntity<ApiResult<TaskResponse>> getTaskByUuid(
            @Parameter(description = "UUID of the task to retrieve",
                    example = "8ee41f14-6bc0-48b9-b07a-1d7971528009",
                    required = true)
            @PathVariable String uuid,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        log.debug("User {} requesting task with UUID: {}", userDetails.getId(), uuid);
        TaskResponse taskResponse = taskQueryService.getTaskByUuid( UUID.fromString(uuid),userDetails.getId());
        log.debug("Successfully retrieved task {} for user {}", uuid, userDetails.getId());
        return ResponseEntity.ok(ApiResult.success(taskResponse));
    }

    @LogActivity(ActionType.VIEW)
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @GetMapping("/myTask")
    @Operation(summary = "Get all user's tasks",
            description = "Get all tasks owned by or assigned to the current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Task found"),
            @ApiResponse(responseCode = "400",
                    description = "Invalid user ID provided"),
            @ApiResponse(responseCode = "404",
                    description = "Task not found"),
            @ApiResponse(responseCode = "403",
                    description = "Forbidden - not allowed to view this task")
    })
    public ResponseEntity<ApiResult<Page<TaskResponse>>> getMyTasks(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @ModelAttribute TaskFilterRequest filter) {
        log.debug("User {} requesting tasks", userDetails.getId());
        Page<TaskResponse> tasks = taskQueryService.getTasksByUserId(userDetails.getId(), filter);
        log.debug("Successfully retrieved tasks for user {}", userDetails.getId());
        return ResponseEntity.ok(ApiResult.success(tasks));
    }

    @LogActivity(ActionType.VIEW)
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN)")
    @GetMapping("/overdue")
    @Operation(summary = "Get all user's overdue tasks",
            description = "Getting all user's overdue tasks except task completed")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Task found"),
            @ApiResponse(responseCode = "400",
                    description = "Invalid user ID provided"),
            @ApiResponse(responseCode = "404",
                    description = "Task not found"),
            @ApiResponse(responseCode = "403",
                    description = "Forbidden - not allowed to view this task")
            })
    public ResponseEntity<ApiResult<Page<TaskResponse>>> getMyOverdueTask(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @ModelAttribute TaskFilterRequest filter
    ){
        log.debug("User {} requesting tasks", userDetails.getId());
        Page<TaskResponse> tasks = taskQueryService.getOverdueTasks(userDetails.getId(), filter);
        log.debug("Successfully retrieved tasks for user {}", userDetails.getId());
        return ResponseEntity.ok(ApiResult.success(tasks));
    }

    @LogActivity(ActionType.VIEW)
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @GetMapping("/search")
    @Operation(summary = "Search tasks",
    description = "Getting user's tasks by keyword and filter")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task Found"),
            @ApiResponse(responseCode = "400", description = "Invalid user ID provided"),
            @ApiResponse(responseCode = "404", description = "Task Not Found"),
            @ApiResponse(responseCode = "403", description = "Forbidden - not allowed to view this task")
    })
    public ResponseEntity<ApiResult<Page<TaskResponse>>> searchTasks(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam String keyword,
            @ModelAttribute TaskFilterRequest filter
    ){
        log.debug("User {} requesting tasks", userDetails.getId());
        Page<TaskResponse> tasks = taskQueryService.searchTasks(userDetails.getId(), keyword, filter);
        log.debug("Successfully retrieved tasks for user {}", userDetails.getId());
        return ResponseEntity.ok(ApiResult.success(tasks));
    }

    @LogActivity(ActionType.VIEW)
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @GetMapping("/status")
    @Operation(summary = "Get tasks by status",
            description = "Getting user's tasks by status and filter")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task Found"),
            @ApiResponse(responseCode = "400", description = "Invalid user ID provided"),
            @ApiResponse(responseCode = "404", description = "Task Not Found"),
            @ApiResponse(responseCode = "403", description = "Forbidden - not allowed to view this task")
    })
    public ResponseEntity<ApiResult<Page<TaskResponse>>> getTasksByStatus(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam Task.TaskStatus status,
            @ModelAttribute TaskFilterRequest filter
            ){
        log.debug("User {} requesting tasks", userDetails.getId());
        Page<TaskResponse> tasks = taskQueryService.getTasksByStatus(userDetails.getId(), status, filter);
        log.debug("Successfully retrieved tasks for user {}", userDetails.getId());
        return ResponseEntity.ok(ApiResult.success(tasks));
    }

    @LogActivity(ActionType.VIEW)
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @GetMapping("/priority")
    @Operation(summary = "Get tasks by priority",
            description = "Getting user's tasks by priority and filter")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task Found"),
            @ApiResponse(responseCode = "400", description = "Invalid user ID provided"),
            @ApiResponse(responseCode = "404", description = "Task Not Found"),
            @ApiResponse(responseCode = "403", description = "Forbidden - not allowed to view this task")
    })
    public ResponseEntity<ApiResult<Page<TaskResponse>>> getTasksByPriority(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam Task.TaskPriority priority,
            @ModelAttribute TaskFilterRequest filter
            ){
        log.debug("User {} requesting tasks", userDetails.getId());
        Page<TaskResponse> tasks = taskQueryService.getTasksByPriority(userDetails.getId(), priority, filter);
        log.debug("Successfully retrieved tasks for user {}", userDetails.getId());
        return ResponseEntity.ok(ApiResult.success(tasks));
    }

    @LogActivity(ActionType.VIEW)
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @GetMapping("/dueToday")
    @Operation(summary = "Get tasks due today",
            description = "Getting user's tasks by due today")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task Found"),
            @ApiResponse(responseCode = "400", description = "Invalid user ID provided"),
            @ApiResponse(responseCode = "404", description = "Task Not Found"),
            @ApiResponse(responseCode = "403", description = "Forbidden - not allowed to view this task")
    })
    public ResponseEntity<ApiResult<Page<TaskResponse>>> getTasksDueToday(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @ModelAttribute TaskFilterRequest filter
            ){
        log.debug("User {} requesting tasks", userDetails.getId());
        Page<TaskResponse> tasks = taskQueryService.getTasksDueToday(userDetails.getId(), filter);
        log.debug("Successfully retrieved tasks for user {}", userDetails.getId());
        return ResponseEntity.ok(ApiResult.success(tasks));
    }

}
