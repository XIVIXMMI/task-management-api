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
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/tasks")
@Slf4j
@Tag(name = "Task Management")
public class TaskQueryController {

    private final TaskQueryService taskQueryService;

    private <T> ResponseEntity<ApiResult<T>> ok(T body) {
        return ResponseEntity.ok(ApiResult.success(body));
    }

    @LogActivity(ActionType.VIEW)
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @GetMapping("/{taskId}")
    @Operation(
            summary = "Get a task by ID",
            description = "- Retrieves a task by its unique identifier with access control validation." +
                    "\n- Fetches a single task including all related entities (assignee, category, workspace) and " +
                    "validates that the requesting user has permission to access the task. " +
                    "\n- The task data is returned as a TaskResponse DTO with complete relationship information." +
                    "\n- Access Control: The user must either be the task owner or assigned to the task. " +
                    "If the user lacks access, a TaskAccessDeniedException is thrown."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task found"),
            @ApiResponse(responseCode = "400", description = "Invalid task ID provided"),
            @ApiResponse(responseCode = "404", description = "Task not found"),
            @ApiResponse(responseCode = "403", description = "Forbidden - not allowed to view this task")
    })
    public ResponseEntity<ApiResult<TaskResponse>> getTaskById(
            @Parameter(description = "ID of the task to retrieve",
                    example = "17",
                    required = true)
            @PathVariable @Positive Long taskId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        log.debug("User {} requesting task with ID: {}", userDetails.getId(), taskId);
        TaskResponse task = taskQueryService.getTaskById(taskId, userDetails.getId());
        log.debug("Successfully retrieved task {} for user {}", taskId, userDetails.getId());
        return ok(task);
    }

    @LogActivity(ActionType.VIEW)
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @GetMapping("/uuid/{uuid}")
    @Operation(
            summary = "Get a task by UUID",
            description = "- Retrieves a task by its UUID with access control validation.\n" +
                    "- Similar to getTaskById(Long, Long) but uses the task's UUID instead of the database ID. " +
                    "UUIDs are typically used in API responses and external integrations to avoid exposing internal " +
                    "database identifiers.\n" +
                    "- Performance Note: UUID lookups may be slightly slower than ID-based lookups depending on database indexing strategy."
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
        TaskResponse task = taskQueryService.getTaskByUuid(UUID.fromString(uuid), userDetails.getId());
        log.debug("Successfully retrieved task {} for user {}", uuid, userDetails.getId());
        return ok(task);
    }

    @LogActivity(ActionType.VIEW)
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @GetMapping("/my")
    @Operation(summary = "Get all user's tasks",
            description = "- Returns all tasks that the user can access (owned or assigned) with support for " +
                    "comprehensive filtering, sorting, and pagination. The filter parameter allows clients " +
                    "to specify criteria such as status, priority, date ranges, and sort preferences.\n" +
                    "- Default Behavior:\n" +
                    "- Includes both owned and assigned tasks\n" +
                    "- Excludes soft-deleted tasks\n" +
                    "- Sorts by creation date descending if no sort specified\n" +
                    "- Uses default page size of 20 if not specified")
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
        return ok(tasks);
    }

    @LogActivity(ActionType.VIEW)
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @GetMapping("/overdue")
    @Operation(summary = "Get all user's overdue tasks",
            description = "- Retrieves a paginated list of overdue tasks for a specific user.\n" +
                    "- Returns tasks where the due date has passed and the task is not yet completed. " +
                    "Only tasks that the user owns or is assigned to are included. \n" +
                    "- Tasks without due dates are excluded from overdue calculations.\n" +
                    "- Overdue Criteria:\n" +
                    "- dueDate is not null and is before current timestamp\n" +
                    "- status is not COMPLETED or CANCELLED\n" +
                    "- task is not soft-deleted")
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
        return ok(tasks);
    }

    @LogActivity(ActionType.VIEW)
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @GetMapping("/search")
    @Operation(summary = "Search tasks",
            description = "- Performs a full-text search across user's tasks with optional additional filtering.\n" +
                    "- Searches task titles, descriptions, and associated metadata for the specified keyword using " +
                    "database full-text search capabilities. The search is case-insensitive and supports partial word matching. " +
                    "Additional filtering can be applied through the filter parameter.\n" +
                    "- Search Scope:\n" +
                    "- Task title (weighted higher in relevance scoring)\n" +
                    "- Task description\n" +
                    "- Category names (if associated)\n" +
                    "- Workspace names (if associated)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task Found"),
            @ApiResponse(responseCode = "400", description = "Invalid user ID provided"),
            @ApiResponse(responseCode = "404", description = "Task Not Found"),
            @ApiResponse(responseCode = "403", description = "Forbidden - not allowed to view this task")
    })
    public ResponseEntity<ApiResult<Page<TaskResponse>>> searchTasks(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam @NotBlank String keyword,
            @ModelAttribute TaskFilterRequest filter
    ){
        log.debug("User {} requesting tasks", userDetails.getId());
        Page<TaskResponse> tasks = taskQueryService.searchTasks(userDetails.getId(), keyword, filter);
        log.debug("Successfully retrieved tasks for user {}", userDetails.getId());
        return ok(tasks);
    }

    @LogActivity(ActionType.VIEW)
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @GetMapping("/status")
    @Operation(summary = "Get tasks by status",
            description = "- Retrieves tasks filtered by a specific completion status.\n" +
                    "- Returns all tasks accessible to the user that match the specified status. " +
                    "This method is useful for dashboard views and status-specific workflows.")
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
        return ok(tasks);
    }

    @LogActivity(ActionType.VIEW)
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @GetMapping("/priority")
    @Operation(summary = "Get tasks by priority",
            description = "- Retrieves tasks filtered by a specific priority level.\n" +
                    "- Returns all tasks accessible to the user that match the specified priority. " +
                    "Useful for priority-based task management and urgent task identification.")
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
        return ok(tasks);
    }

    @LogActivity(ActionType.VIEW)
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @GetMapping("/dueToday")
    @Operation(summary = "Get tasks due today",
            description = "- Retrieves tasks that are due today for a specific user.\n" +
                    "- Returns tasks where the due date falls within the current calendar day (midnight to 23:59:59) " +
                    "in the system's default timezone. Only incomplete tasks are included in the results.\n" +
                    "- Due Today Criteria:\n" +
                    "- dueDate is within today's date range\n" +
                    "- status is not COMPLETED or CANCELLED\n" +
                    "- task is not soft-deleted")
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
        return ok(tasks);
    }

    @LogActivity(ActionType.VIEW)
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @GetMapping("/due")
    @Operation(summary = "Get tasks with due dates within a specific date range.",
            description = "- Retrieves tasks with due dates within a specific date range.\n" +
                    "- Returns tasks where the due date falls between the specified start and end timestamps (inclusive)." +
                    " This method is useful for calendar views and date-range based task planning.\n" +
                    "- Date Range Behavior:\n" +
                    "- start and end timestamps are inclusive\n" +
                    "- Tasks with null due dates are excluded\n" +
                    "- Results include all task statuses unless filtered otherwise")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task Found"),
            @ApiResponse(responseCode = "400", description = "Invalid user ID provided"),
            @ApiResponse(responseCode = "404", description = "Task Not Found"),
            @ApiResponse(responseCode = "403", description = "Forbidden - not allowed to view this task")
    })
    public ResponseEntity<ApiResult<Page<TaskResponse>>> getTasksDueBetween(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "the start of the date range (inclusive)",
                    example = "2025-09-30T09:30:00",
                    required = true)
            @RequestParam String start,
            @Parameter(description = "the end of the date range (inclusive)",
                    example = "2025-12-31T09:30:00",
                    required = true)
            @RequestParam String end,
            @ModelAttribute TaskFilterRequest filter
    ) {
        log.debug("User {} requesting tasks", userDetails.getId());
        LocalDateTime startDate = LocalDateTime.parse(start);
        LocalDateTime endDate = LocalDateTime.parse(end);

        Page<TaskResponse> tasks = taskQueryService.getTasksDueBetween(
                userDetails.getId(),
                startDate,
                endDate,
                filter);
        log.debug("Successfully retrieved tasks for user {}", userDetails.getId());
        return ok(tasks);
    }

    @LogActivity(ActionType.VIEW)
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @GetMapping("/{daysBack}/recentlyUpdated")
    @Operation(summary = "Get recently tasks updated",
            description = "- Retrieves tasks that have been updated within a specified number of days.\n" +
                    "- Returns tasks that have been modified (any field change) within the last N days " +
                    "from the current timestamp. This includes creation, updates, status changes, " +
                    "and progress modifications. Useful for activity tracking and recent changes review.\n" +
                    "- Update Tracking:\n" +
                    "- Based on task's updatedAt timestamp\n" +
                    "- Includes all types of modifications\n" +
                    "- Ordered by most recently updated first")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task Found"),
            @ApiResponse(responseCode = "400", description = "Invalid user ID provided"),
            @ApiResponse(responseCode = "404", description = "Task Not Found"),
            @ApiResponse(responseCode = "403", description = "Forbidden - not allowed to view this task")
    })
    public ResponseEntity<ApiResult<Page<TaskResponse>>> getRecentlyUpdatedTasks(
            @Parameter(description = "The number of days to look back for updates (must be positive)",
                    example = "30",
                    required = true)
            @PathVariable @Positive Integer daysBack,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @ModelAttribute TaskFilterRequest filter
    ) {
        log.debug("User {} requesting tasks", userDetails.getId());
        Page<TaskResponse> tasks = taskQueryService.getRecentlyUpdatedTasks(userDetails.getId(), daysBack, filter);
        log.debug("Successfully retrieved tasks for user {}", userDetails.getId());
        return ok(tasks);
    }

}
