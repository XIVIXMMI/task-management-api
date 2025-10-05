package com.omori.taskmanagement.controller.task;

import com.omori.taskmanagement.annotations.LogActivity;
import com.omori.taskmanagement.controller.BaseController;
import com.omori.taskmanagement.dto.common.ApiResult;
import com.omori.taskmanagement.dto.project.task.HierarchyEpicDto;
import com.omori.taskmanagement.dto.project.task.TaskFilterRequest;
import com.omori.taskmanagement.dto.project.task.TaskResponse;
import com.omori.taskmanagement.model.audit.ActionType;
import com.omori.taskmanagement.model.project.Task;
import com.omori.taskmanagement.security.service.CustomUserDetails;
import com.omori.taskmanagement.service.task.hierarchy.TaskHierarchyService;
import com.omori.taskmanagement.service.task.query.TaskQueryService;
import com.omori.taskmanagement.service.task.update.TaskProgressService;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/tasks")
@Slf4j
@Tag(name = "Task Management")
public class TaskQueryController extends BaseController {

    private final TaskQueryService taskQueryService;
    private final TaskProgressService progressService;
    private final TaskHierarchyService hierarchyService;

    @LogActivity(ActionType.VIEW)
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @GetMapping("/{taskId}")
    @Operation(
            summary = "Get a task by ID",
            description = """
                    Retrieves a single task by its unique database identifier with access control validation.

                    What is Retrieved:
                    - Complete task information including all fields
                    - Related entities: assignee user, category, workspace, parent task
                    - Child task references (if applicable)
                    - Subtask information
                    - Timestamps: createdAt, updatedAt, completedAt
                    - Progress and status information

                    Access Control:
                    User must satisfy ONE of these conditions:
                    - User is the task owner (createdBy equals userId)
                    - User is assigned to the task (assignedTo equals userId)
                    - User has ADMIN role

                    If access is denied: TaskAccessDeniedException is thrown (403 Forbidden)

                    Use Cases:
                    - Retrieving task details for display in UI
                    - Loading task for editing
                    - Fetching task information for reporting
                    - API integration task lookups

                    Performance:
                    - Single optimized database query with JOIN FETCH for relations
                    - Cached responses (if caching enabled)
                    - Typical response time: 20 to 100 milliseconds

                    Response:
                    Returns TaskResponse DTO with complete task data and all related entity information.

                    Error Handling:
                    - 400 Bad Request: Invalid task ID format (non-numeric)
                    - 403 Forbidden: User does not have access to view this task
                    - 404 Not Found: Task does not exist or has been soft-deleted

                    Note:
                    Soft-deleted tasks are not returned. Use admin endpoints to retrieve deleted tasks.
                    """
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
        return executeMethod(
                userDetails.getId(),
                "GET_TASK_BY_ID",
                () -> taskQueryService.getTaskById(taskId, userDetails.getId())
        );
    }

    @LogActivity(ActionType.VIEW)
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @GetMapping("/uuid/{uuid}")
    @Operation(
            summary = "Get a task by UUID",
            description = """
                    Retrieves a single task by its universally unique identifier (UUID) with access control validation.

                    UUID vs ID:
                    - UUID: Globally unique identifier, safe for external exposure, used in API responses
                    - ID: Internal database identifier, may be sequential, used for internal operations
                    - This endpoint uses UUID for security and external integration purposes

                    What is Retrieved:
                    Same as getTaskById: complete task information with all related entities, subtasks,
                    timestamps, and relationship information.

                    Access Control:
                    Same as getTaskById: user must be owner, assignee, or admin.

                    Use Cases:
                    - External API integrations using UUID-based references
                    - Public API endpoints where database IDs should not be exposed
                    - Cross-system task references
                    - Webhook callbacks using UUID identifiers

                    Performance:
                    - Single database query with UUID index lookup
                    - UUID lookups may be slightly slower than ID lookups (depends on database indexing)
                    - Typical response time: 30 to 150 milliseconds
                    - Cached responses (if caching enabled)

                    Response:
                    Returns TaskResponse DTO identical to ID-based lookup.

                    Error Handling:
                    - 400 Bad Request: Invalid UUID format
                    - 403 Forbidden: User does not have access to view this task
                    - 404 Not Found: Task with given UUID does not exist or has been soft-deleted

                    Best Practice:
                    Use UUID for external APIs and public-facing endpoints. Use ID for internal operations
                    where performance is critical.
                    """
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
            @PathVariable UUID uuid,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return executeMethod(
                userDetails.getId(),
                "GET_TASK_BY_UUID",
                () -> taskQueryService.getTaskByUuid(uuid, userDetails.getId())
        );
    }

    @LogActivity(ActionType.VIEW)
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @GetMapping("/my")
    @Operation(summary = "Get all tasks accessible to the user",
            description = """
                    Returns all tasks that the user can access (owned or assigned) with comprehensive
                    filtering, sorting, and pagination support.

                    What is Retrieved:
                    - All tasks where user is owner (createdBy equals userId)
                    - All tasks where user is assignee (assignedTo equals userId)
                    - Excludes soft-deleted tasks automatically
                    - Complete task information with related entities

                    Filtering Capabilities (via TaskFilterRequest):
                    - status: Filter by task status (PENDING, IN_PROGRESS, COMPLETED, etc.)
                    - priority: Filter by priority level (LOW, MEDIUM, HIGH, CRITICAL)
                    - categoryId: Filter tasks in specific category
                    - workspaceId: Filter tasks in specific workspace
                    - dueDateFrom and dueDateTo: Filter by due date range
                    - assignedToId: Filter tasks assigned to specific user
                    - taskType: Filter by type (EPIC, STORY, TASK)
                    - searchKeyword: Filter by text in title or description

                    Sorting Options:
                    - Sort by: title, status, priority, dueDate, createdAt, updatedAt, progress
                    - Direction: ascending (ASC) or descending (DESC)
                    - Default: createdAt descending (newest first)

                    Pagination:
                    - page: Page number (0-based, default 0)
                    - size: Page size (default 20, max 100)
                    - Returns Spring Page object with content, total elements, total pages

                    Default Behavior:
                    - Includes both owned and assigned tasks
                    - Excludes soft-deleted tasks
                    - Sorts by createdAt descending
                    - Page size 20 if not specified
                    - Page 0 (first page) if not specified

                    Use Cases:
                    - Dashboard showing user tasks
                    - My Tasks view in UI
                    - Task list with filters and sorting
                    - Mobile app task synchronization

                    Performance:
                    - Optimized query with appropriate indexes
                    - Pagination limits result set size
                    - Typical response time: 50 to 200 milliseconds
                    - For large datasets, consider using additional filters

                    Response:
                    Returns Page of TaskResponse with:
                    - content: List of TaskResponse objects
                    - totalElements: Total number of tasks matching criteria
                    - totalPages: Total pages available
                    - number: Current page number
                    - size: Page size
                    - Pagination metadata for UI controls

                    Example Request:
                    GET /api/v1/tasks/my?status=IN_PROGRESS&priority=HIGH&page=0&size=20&sort=dueDate,asc

                    Error Handling:
                    - 400 Bad Request: Invalid filter parameters or pagination values
                    - 403 Forbidden: Authentication failed
                    """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Tasks retrieved successfully"),
            @ApiResponse(responseCode = "400",
                    description = "Invalid filter or pagination parameters"),
            @ApiResponse(responseCode = "403",
                    description = "Forbidden - authentication required")
    })
    public ResponseEntity<ApiResult<Page<TaskResponse>>> getMyTasks(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @ModelAttribute TaskFilterRequest filter) {
        return executeMethod(
                userDetails.getId(),
                "GET_MY_TASKS",
                () -> taskQueryService.getTasksByUserId(userDetails.getId(), filter)
        );
    }

    @LogActivity(ActionType.VIEW)
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @GetMapping("/overdue")
    @Operation(summary = "Get all overdue tasks for the user",
            description = """
                    Retrieves a paginated list of overdue tasks where the due date has passed
                    and the task is not yet completed. \s

                    Overdue Criteria:
                    A task is considered overdue when ALL of these conditions are met:
                    - dueDate is not null
                    - dueDate is before current timestamp
                    - status is not COMPLETED or CANCELLED
                    - task is not soft-deleted
                    - user is owner or assignee

                    What is Retrieved:
                    - All overdue tasks accessible to the user
                    - Complete task information with related entities
                    - Excludes tasks without due dates
                    - Excludes completed or cancelled tasks

                    Additional Filtering:
                    - Supports all TaskFilterRequest filters (priority, category, workspace, etc.)
                    - Supports sorting and pagination
                    - Default sort: dueDate ascending (most overdue first)

                    Use Cases:
                    - Overdue tasks dashboard widget
                    - Priority task list requiring immediate attention
                    - Task management reports
                    - Notification systems for overdue tasks

                    Response:
                    Returns paginated list of overdue TaskResponse objects sorted by urgency.

                    Performance:
                    - Indexed query on dueDate and status
                    - Typical response time: 50 to 150 milliseconds
                    """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Overdue tasks retrieved successfully"),
            @ApiResponse(responseCode = "400",
                    description = "Invalid filter or pagination parameters"),
            @ApiResponse(responseCode = "403",
                    description = "Forbidden - authentication required")
            })
    public ResponseEntity<ApiResult<Page<TaskResponse>>> getMyOverdueTask(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @ModelAttribute TaskFilterRequest filter
    ){
        return executeMethod(
                userDetails.getId(),
                "GET_MY_OVERDUE_TASKS",
                () -> taskQueryService.getOverdueTasks(userDetails.getId(), filter)
        );
    }

    @LogActivity(ActionType.VIEW)
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @GetMapping("/search")
    @Operation(summary = "Search tasks by keyword",
            description = """
                    Performs full-text search across user tasks with optional additional filtering.
                    Searches task titles, descriptions, and metadata using case-insensitive partial matching.

                    Search Scope:
                    - Task title (weighted higher in relevance scoring)
                    - Task description
                    - Category names (if associated)
                    - Workspace names (if associated)
                    - Metadata fields

                    Search Behavior:
                    - Case-insensitive matching
                    - Partial word matching supported
                    - Uses database full-text search capabilities
                    - Results sorted by relevance score (best matches first)

                    Additional Filtering:
                    Can be combined with TaskFilterRequest filters for refined results:
                    - status, priority, category, workspace filters
                    - date range filters
                    - pagination and sorting

                    Use Cases:
                    - Task search bar in UI
                    - Quick task lookup by name or description
                    - Finding tasks by category or workspace name
                    - Searching task metadata

                    Performance:
                    - Full-text search index used for optimal performance
                    - Response time depends on result set size
                    - Typical response time: 100 to 300 milliseconds

                    Example:
                    GET /api/v1/tasks/search?keyword=authentication&status=IN_PROGRESS

                    Response:
                    Returns paginated list of matching TaskResponse objects sorted by relevance.
                    """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Search completed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid keyword or filter parameters"),
            @ApiResponse(responseCode = "403", description = "Forbidden - authentication required")
    })
    public ResponseEntity<ApiResult<Page<TaskResponse>>> searchTasks(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam @NotBlank String keyword,
            @ModelAttribute TaskFilterRequest filter
    ){
        return executeMethod(
                userDetails.getId(),
                "SEARCH_TASK",
                () -> taskQueryService.searchTasks(userDetails.getId(), keyword, filter)
        );
    }

    @LogActivity(ActionType.VIEW)
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @GetMapping("/status")
    @Operation(summary = "Get tasks by status",
            description = """
                    Retrieves tasks filtered by a specific task status with pagination support.

                    Status Values:
                    - PENDING: Task not started
                    - IN_PROGRESS: Task currently being worked on
                    - COMPLETED: Task finished
                    - ON_HOLD: Task paused
                    - CANCELLED: Task cancelled

                    What is Retrieved:
                    - All tasks accessible to user matching the specified status
                    - Complete task information with related entities
                    - Excludes soft-deleted tasks

                    Additional Filtering:
                    - Can combine with other filters (priority, category, dates)
                    - Supports sorting and pagination
                    - Default sort: createdAt descending

                    Use Cases:
                    - Dashboard status columns (Kanban board)
                    - Status-specific task lists (In Progress, Completed)
                    - Workflow management and status tracking
                    - Progress reports by status

                    Example:
                    GET /api/v1/tasks/status?status=IN_PROGRESS&page=0&size=20

                    Response:
                    Returns paginated list of TaskResponse objects matching the status.
                    """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tasks retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid status or filter parameters"),
            @ApiResponse(responseCode = "403", description = "Forbidden - authentication required")
    })
    public ResponseEntity<ApiResult<Page<TaskResponse>>> getTasksByStatus(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam Task.TaskStatus status,
            @ModelAttribute TaskFilterRequest filter
            ){
        return executeMethod(
                userDetails.getId(),
                "GET_TASKS_BY_STATUS",
                () -> taskQueryService.getTasksByStatus(userDetails.getId(), status, filter)
        );
    }

    @LogActivity(ActionType.VIEW)
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @GetMapping("/priority")
    @Operation(summary = "Get tasks by priority",
            description = """
                    Retrieves tasks filtered by a specific priority level with pagination support.

                    Priority Levels:
                    - LOW: Low priority tasks
                    - MEDIUM: Medium priority tasks (default)
                    - HIGH: High priority tasks
                    - CRITICAL: Critical or urgent tasks

                    What is Retrieved:
                    - All tasks accessible to user matching the specified priority
                    - Complete task information with related entities
                    - Excludes soft-deleted tasks

                    Additional Filtering:
                    - Can combine with other filters (status, category, dates)
                    - Supports sorting and pagination
                    - Default sort: dueDate ascending (most urgent first)

                    Use Cases:
                    - Priority-based task management
                    - Urgent task identification
                    - High priority task dashboard
                    - Task prioritization and planning

                    Example:
                    GET /api/v1/tasks/priority?priority=HIGH&status=IN_PROGRESS

                    Response:
                    Returns paginated list of TaskResponse objects matching the priority level.
                    """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tasks retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid priority or filter parameters"),
            @ApiResponse(responseCode = "403", description = "Forbidden - authentication required")
    })
    public ResponseEntity<ApiResult<Page<TaskResponse>>> getTasksByPriority(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam Task.TaskPriority priority,
            @ModelAttribute TaskFilterRequest filter
            ){
        return executeMethod(
                userDetails.getId(),
                "GET_TASKS_BY_PRIORITY",
                () -> taskQueryService.getTasksByPriority(userDetails.getId(), priority, filter)
        );
    }

    @LogActivity(ActionType.VIEW)
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @GetMapping("/dueToday")
    @Operation(summary = "Get tasks due today",
            description = """
                    Retrieves tasks that are due today for the authenticated user.

                    Due Today Criteria:
                    A task is considered due today when ALL of these conditions are met:
                    - dueDate falls within the current calendar day (midnight to 23:59:59)
                    - Timezone: System default timezone
                    - status is not COMPLETED or CANCELLED
                    - task is not soft-deleted
                    - user is owner or assignee

                    What is Retrieved:
                    - All incomplete tasks with due date today
                    - Complete task information with related entities
                    - Excludes tasks without due dates
                    - Excludes completed or cancelled tasks

                    Additional Filtering:
                    - Supports all TaskFilterRequest filters
                    - Supports sorting and pagination
                    - Default sort: priority descending then dueDate ascending

                    Use Cases:
                    - Today task list dashboard
                    - Daily task planning
                    - Daily standup preparation
                    - Task reminder systems

                    Example:
                    GET /api/v1/tasks/dueToday?page=0&size=20

                    Response:
                    Returns paginated list of TaskResponse objects due today.
                    """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tasks retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid filter or pagination parameters"),
            @ApiResponse(responseCode = "403", description = "Forbidden - authentication required")
    })
    public ResponseEntity<ApiResult<Page<TaskResponse>>> getTasksDueToday(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @ModelAttribute TaskFilterRequest filter
            ){
        return executeMethod(
                userDetails.getId(),
                "GET_TASKS_DUE_TODAY",
                () -> taskQueryService.getTasksDueToday(userDetails.getId(), filter)
        );
    }

    @LogActivity(ActionType.VIEW)
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @GetMapping("/due")
    @Operation(summary = "Get tasks with due dates within a specific date range",
            description = """
                    Retrieves tasks with due dates falling within a specified date range (inclusive).

                    Date Range Behavior:
                    - start and end timestamps are inclusive (tasks due on start date or end date are included)
                    - Tasks with null due dates are excluded
                    - Results include all task statuses unless filtered otherwise
                    - Date format: ISO 8601 LocalDateTime (example: 2025-09-30T09:30:00)

                    What is Retrieved:
                    - All tasks with dueDate between start and end (inclusive)
                    - Tasks accessible to user (owned or assigned)
                    - Complete task information with related entities
                    - Excludes soft-deleted tasks

                    Additional Filtering:
                    - Can combine with other filters (status, priority, category)
                    - Supports sorting and pagination
                    - Default sort: dueDate ascending

                    Use Cases:
                    - Calendar views showing tasks in date range
                    - Weekly or monthly task planning
                    - Sprint planning (tasks due in sprint period)
                    - Deadline tracking and forecasting

                    Example:
                    GET /api/v1/tasks/due?start=2025-09-30T00:00:00&end=2025-12-31T23:59:59&page=0&size=20

                    Validation:
                    - start parameter is required
                    - end parameter is required
                    - start must be before or equal to end
                    - Invalid date format returns 400 Bad Request

                    Response:
                    Returns paginated list of TaskResponse objects with due dates in the specified range.
                    """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tasks retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid date format or date range parameters"),
            @ApiResponse(responseCode = "403", description = "Forbidden - authentication required")
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
        return executeMethod(
                userDetails.getId(),
                "GET_TASKS_DUE_BETWEEN",
                () -> taskQueryService.getTasksDueBetween(
                        userDetails.getId(),
                        LocalDateTime.parse(start),
                        LocalDateTime.parse(end),
                        filter)
        );
    }

    @LogActivity(ActionType.VIEW)
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @GetMapping("/{daysBack}/recentlyUpdated")
    @Operation(summary = "Get recently updated tasks",
            description = """
                    Retrieves tasks that have been updated within a specified number of days from now.

                    Update Tracking:
                    - Based on task updatedAt timestamp
                    - Includes all types of modifications: field changes, status changes, reassignments, etc.
                    - Time window: Current timestamp minus specified daysBack
                    - Ordered by most recently updated first (default)

                    What is Retrieved:
                    - All tasks modified within the last N days
                    - Tasks accessible to user (owned or assigned)
                    - Complete task information with related entities
                    - Includes newly created tasks (createdAt equals updatedAt initially)
                    - Excludes soft-deleted tasks

                    Additional Filtering:
                    - Can combine with other filters (status, priority, category)
                    - Supports sorting and pagination
                    - Default sort: updatedAt descending (most recent first)

                    Use Cases:
                    - Activity tracking and recent changes review
                    - What Changed dashboard widget
                    - Monitoring task updates and progress
                    - Audit and activity logs
                    - Team collaboration tracking

                    Parameter Validation:
                    - daysBack must be positive integer
                    - Example: daysBack equals 7 returns tasks updated in last 7 days
                    - Example: daysBack equals 30 returns tasks updated in last 30 days

                    Example:
                    GET /api/v1/tasks/7/recentlyUpdated?page=0&size=20

                    Response:
                    Returns paginated list of TaskResponse objects ordered by most recent updates.

                    Performance:
                    - Indexed query on updatedAt timestamp
                    - Efficient for reasonable time windows (up to 90 days)
                    - Typical response time: 50 to 200 milliseconds
                    """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tasks retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid daysBack parameter (must be positive)"),
            @ApiResponse(responseCode = "403", description = "Forbidden - authentication required")
    })
    public ResponseEntity<ApiResult<Page<TaskResponse>>> getRecentlyUpdatedTasks(
            @Parameter(description = "The number of days to look back for updates (must be positive)",
                    example = "30",
                    required = true)
            @PathVariable @Positive Integer daysBack,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @ModelAttribute TaskFilterRequest filter
    ) {
        return executeMethod(
                userDetails.getId(),
                "GET_RECENTLY_UPDATED_TASKS",
                () -> taskQueryService.getRecentlyUpdatedTasks(userDetails.getId(), daysBack, filter)
        );
    }

    // ========== PROGRESS QUERY ENDPOINTS ==========

    @LogActivity(ActionType.VIEW)
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @GetMapping("/progress/{taskId}")
    @Operation(summary = "Calculate task progress percentage without updating database",
            description = """
                Calculates and returns the current progress percentage for a task based on its completed subtasks.
                This is a READ-ONLY operation that does not modify any data in the database.

                Calculation Formula:
                    progress = (completed_subtasks / total_subtasks) × 100

                Return Values:
                - Returns 0 if the task has no subtasks
                - Returns percentage (0-100) representing subtask completion ratio

                Scope:
                - Only calculates from DIRECT subtasks of the specified task
                - Does NOT traverse child tasks in the hierarchy (Task → Story → Epic)
                - Does NOT consider manual progress values set on the task

                Use Cases:
                - Quick progress check without database modification
                - Preview progress calculation before applying updates
                - API clients needing real-time progress without side effects
                - Dashboard widgets displaying current task completion

                Hierarchy Awareness:
                This method is NOT hierarchy-aware. It only looks at immediate subtasks.
                For hierarchy-aware progress (Epic → Stories → Tasks), use:
                - recalculateTaskProgress() - Updates task and propagates to parents
                - refreshEpicProgress() - Recalculates entire Epic hierarchy

                Example Response:
                - Task with 3 completed subtasks out of 5 total → returns 60
                - Task with no subtasks → returns 0
                - Task with all subtasks completed → returns 100
                """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Progress calculated successfully"),
            @ApiResponse(responseCode = "404", description = "Task not found")
    })
    public ResponseEntity<ApiResult<Integer>> calculateTaskProgress(
            @Parameter(description = "ID of the task to calculate",
                    example = "42",
                    required = true)
            @PathVariable Long taskId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return executeMethod(
                userDetails.getId(),
                "CALCULATE_TASK_PROGRESS",
                () -> progressService.calculateTaskProgress(taskId)
        );
    }

    // ========== HIERARCHY QUERY ENDPOINTS ==========

    @LogActivity(ActionType.VIEW)
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @GetMapping("/hierarchy/{taskId}")
    @Operation(summary = "Get complete Epic hierarchy structure",
            description = """
            Retrieves the complete hierarchy structure for an Epic task including all child Stories,
            Tasks under those Stories, and all Subtasks at every level.

            Hierarchy Structure Returned:
                Epic (with subtasks)
                    Story 1 (with subtasks)
                        Task 1.1 (with subtasks)
                        Task 1.2 (with subtasks)
                    Story 2 (with subtasks)
                        Task 2.1 (with subtasks)
                        Task 2.2 (with subtasks)

            What is Retrieved:
            - Complete Epic information with all fields
            - All child Stories under the Epic
            - All Tasks under each Story
            - All Subtasks at Epic, Story, and Task levels
            - Related entities: assignee, category, workspace names
            - Progress and status information at all levels

            Performance:
            - Single optimized query loads entire hierarchy
            - Subtasks bulk-loaded in one query
            - Typical response time: 100 to 300 milliseconds
            - Efficient for Epics with up to 50 Stories

            Use Cases:
            - Epic roadmap visualization
            - Complete project overview dashboards
            - Hierarchy tree views
            - Sprint planning and Epic breakdown
            - Progress tracking across entire Epic

            Validation:
            - Task ID must reference an existing Epic (type EPIC)
            - If task is not type EPIC, returns 400 Bad Request
            - Only non-deleted tasks are included

            Response Structure:
            Returns HierarchyEpicDto containing:
            - epic: Complete Epic details with subtasks
            - stories: List of StoryWithTaskDto objects
                - story: Story details with subtasks
                - tasks: List of Task details with subtasks

            Error Handling:
            - 400 Bad Request: Task is not type EPIC
            - 404 Not Found: Epic with given ID does not exist
            - 403 Forbidden: User does not have access to Epic workspace

            Example:
            GET /api/v1/tasks/hierarchy/17
            """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Hierarchy retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Task is not type EPIC"),
            @ApiResponse(responseCode = "404", description = "Epic not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<ApiResult<HierarchyEpicDto>> getFullHierarchy(
            @Parameter(description = "ID of the Epic task",
                    example = "17",
                    required = true)
            @PathVariable Long taskId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return executeMethod(
                userDetails.getId(),
                "GET_FULL_HIERARCHY_EPIC",
                () -> hierarchyService.getFullHierarchy(taskId)
        );
    }

    @LogActivity(ActionType.VIEW)
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @GetMapping("/hierarchy/uuid/{uuid}")
    @Operation(summary = "Get complete Epic hierarchy structure by UUID",
            description = """
            Retrieves the complete hierarchy structure for an Epic task using UUID instead of database ID.
            Functionally identical to getFullHierarchy but uses UUID for external API integrations.

            UUID vs ID:
            - UUID: Globally unique identifier, safe for external exposure
            - ID: Internal database identifier, sequential
            - Use UUID for public APIs and external integrations
            - Use ID for internal operations where performance is critical

            Hierarchy Structure Returned:
                Epic (with subtasks)
                    Story 1 (with subtasks)
                        Task 1.1 (with subtasks)
                        Task 1.2 (with subtasks)
                    Story 2 (with subtasks)
                        Task 2.1 (with subtasks)

            What is Retrieved:
            Same as getFullHierarchy: complete Epic with all Stories, Tasks, and Subtasks
            at every level, including all related entity information.

            Performance:
            - Single optimized query with UUID index lookup
            - UUID lookup slightly slower than ID lookup (negligible difference)
            - Typical response time: 100 to 350 milliseconds
            - Subtasks bulk-loaded efficiently

            Use Cases:
            - External API integrations using UUID references
            - Public-facing APIs where database IDs should not be exposed
            - Webhook callbacks with UUID identifiers
            - Cross-system Epic references
            - Mobile apps using UUID-based caching

            Validation:
            - UUID must be valid format (8-4-4-4-12 hexadecimal)
            - Epic with given UUID must exist
            - Task must be type EPIC
            - Only non-deleted tasks included

            Response Structure:
            Returns HierarchyEpicDto identical to ID-based endpoint:
            - epic: Complete Epic details with subtasks
            - stories: List of StoryWithTaskDto objects

            Error Handling:
            - 400 Bad Request: Invalid UUID format or task is not type EPIC
            - 404 Not Found: Epic with given UUID does not exist
            - 403 Forbidden: User does not have access to Epic workspace

            Example:
            GET /api/v1/tasks/hierarchy/uuid/8ee41f14-6bc0-48b9-b07a-1d7971528009

            Best Practice:
            Use this endpoint for external APIs. Use ID-based endpoint for internal operations.
            """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Hierarchy retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid UUID or task is not type EPIC"),
            @ApiResponse(responseCode = "404", description = "Epic not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<ApiResult<HierarchyEpicDto>> getFullHierarchyByUuid(
            @Parameter(description = "UUID of the Epic task",
                    example = "8ee41f14-6bc0-48b9-b07a-1d7971528009",
                    required = true)
            @PathVariable String uuid
            ) {
        HierarchyEpicDto tasks = hierarchyService.getFullHierarchyByUuid(uuid);
        return ResponseEntity.ok(ApiResult.success(tasks));
    }

    @LogActivity(ActionType.VIEW)
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @GetMapping("/hierarchy/{parentTaskId}/children")
    @Operation(summary = "Get direct child tasks (one level down) of a parent task",
            description = """
            Retrieves only the immediate children (one level down) of the specified parent task.
            Does not traverse deeper levels of the hierarchy.

            Parent-Child Relationships:
            - If parent is EPIC: returns all STORY tasks under the Epic
            - If parent is STORY: returns all TASK tasks under the Story
            - If parent is TASK: returns empty list (Tasks have subtasks, not child tasks)

            Hierarchy Context:
                Epic (parentTaskId)
                    Story 1 (returned)
                    Story 2 (returned)
                    Story 3 (returned)

            Or:
                Story (parentTaskId)
                    Task 1 (returned)
                    Task 2 (returned)
                    Task 3 (returned)

            What is Retrieved:
            - List of direct child tasks only (one level)
            - Complete task information with all fields
            - Related entities: assignee, category, workspace names
            - Progress and status information
            - Excludes soft-deleted tasks

            Use Cases:
            - Expandable tree views with lazy loading
            - Kanban board column population (Stories under Epic)
            - Dropdown lists for task selection
            - Navigation breadcrumb building
            - Parent-child relationship visualization

            Performance:
            - Single optimized database query
            - No recursive traversal
            - Typical response time: 30 to 100 milliseconds
            - Efficient for any hierarchy level

            Sorting:
            - Results sorted by sortOrder field (ascending)
            - Maintains manual task ordering set by users
            - Can be re-ordered using update endpoints

            Empty Results:
            - Returns empty list if parent has no children
            - Returns empty list if parent is type TASK
            - Does not throw error for childless parents

            Comparison with getAllChildTasks:
            - getChildTasks: Only immediate children (one level)
            - getAllChildTasks: All descendants recursively (entire subtree)

            Example Response for Epic parent:
            Returns list of Story tasks:
            [
              {"id": 20, "title": "User Authentication", "taskType": "STORY", ...},
              {"id": 21, "title": "Dashboard UI", "taskType": "STORY", ...}
            ]

            Example Response for Story parent:
            Returns list of Task tasks:
            [
              {"id": 30, "title": "Login Page", "taskType": "TASK", ...},
              {"id": 31, "title": "Logout API", "taskType": "TASK", ...}
            ]

            Error Handling:
            - 404 Not Found: Parent task does not exist
            - 403 Forbidden: User does not have access to parent task workspace

            Example:
            GET /api/v1/tasks/hierarchy/17/children
            """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Child tasks retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Parent task not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<ApiResult<List<TaskResponse>>> getChildTasks(
            @Parameter(description = "ID of the parent task",
                    example = "17",
                    required = true)
            @PathVariable Long parentTaskId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return executeMethod(
                userDetails.getId(),
                "GET_CHILD_TASKS",
                () -> hierarchyService.getChildTasks(parentTaskId)
        );
    }

    @LogActivity(ActionType.VIEW)
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @GetMapping("/hierarchy/{parentTaskId}/descendants")
    @Operation(summary = "Get all descendant tasks recursively (entire subtree)",
            description = """
            Retrieves all descendant tasks recursively, traversing the entire subtree below
            the specified parent task. Returns a flattened list of all descendants regardless of depth.

            Recursive Traversal:
            - If parent is EPIC: returns all Stories AND all Tasks under those Stories
            - If parent is STORY: returns all Tasks under the Story
            - If parent is TASK: returns empty list (Tasks have no child tasks)

            Hierarchy Context for Epic parent:
                Epic (parentTaskId)
                    Story 1 (returned)
                        Task 1.1 (returned)
                        Task 1.2 (returned)
                    Story 2 (returned)
                        Task 2.1 (returned)
                        Task 2.2 (returned)

            Result is flattened list:
            [Story1, Task1.1, Task1.2, Story2, Task2.1, Task2.2]

            What is Retrieved:
            - All descendant tasks at all levels (flattened)
            - Complete task information for each descendant
            - Related entities: assignee, category, workspace names
            - Progress and status information
            - Excludes soft-deleted tasks
            - Parent task itself is NOT included

            Use Cases:
            - Bulk operations on entire subtree (mark all as completed)
            - Complete subtree export for reporting
            - Dependency tracking across hierarchy
            - Calculating total work in Epic (sum all Tasks)
            - Searching within Epic scope
            - Migration or archival operations

            Performance:
            - For Epic parent: Single optimized query loads all descendants
            - For Story parent: Single query for all child Tasks
            - Typical response time: 50 to 200 milliseconds
            - Efficient for Epics with up to 100 total descendants
            - Consider pagination for very large hierarchies

            Sorting:
            - Results are NOT hierarchically structured
            - Flattened list in database order
            - For hierarchical structure, use getFullHierarchy instead

            Empty Results:
            - Returns empty list if parent has no descendants
            - Returns empty list if parent is type TASK
            - Does not throw error for childless parents

            Comparison with getChildTasks:
            - getChildTasks: Only immediate children (one level)
            - getAllChildTasks: All descendants recursively (entire subtree)

            Comparison with getFullHierarchy:
            - getFullHierarchy: Returns hierarchical structure (nested)
            - getAllChildTasks: Returns flattened list (no nesting)

            Example Response for Epic parent:
            Returns flattened list of all Stories and Tasks:
            [
              {"id": 20, "title": "User Auth", "taskType": "STORY", ...},
              {"id": 30, "title": "Login Page", "taskType": "TASK", ...},
              {"id": 31, "title": "Logout API", "taskType": "TASK", ...},
              {"id": 21, "title": "Dashboard", "taskType": "STORY", ...},
              {"id": 32, "title": "Chart Widget", "taskType": "TASK", ...}
            ]

            Example Response for Story parent:
            Returns flattened list of all Tasks:
            [
              {"id": 30, "title": "Login Page", "taskType": "TASK", ...},
              {"id": 31, "title": "Logout API", "taskType": "TASK", ...},
              {"id": 32, "title": "Password Reset", "taskType": "TASK", ...}
            ]

            Error Handling:
            - 404 Not Found: Parent task does not exist
            - 403 Forbidden: User does not have access to parent task workspace

            Example:
            GET /api/v1/tasks/hierarchy/17/descendants

            Warning:
            This endpoint returns potentially large result sets. For very large Epics (100 plus descendants),
            consider using getFullHierarchy with pagination or filtering at client side.
            """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Descendant tasks retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Parent task not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<ApiResult<List<TaskResponse>>> getAllChildTasks(
            @Parameter(description = "ID of the parent task",
                    example = "17",
                    required = true)
            @PathVariable Long parentTaskId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return executeMethod(
                userDetails.getId(),
                "GET_ALL_CHILD_TASKS",
                () -> hierarchyService.getAllChildTasks(parentTaskId)
        );
    }

    @LogActivity(ActionType.VIEW)
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @GetMapping("/hierarchy/{taskId}/parent")
    @Operation(summary = "Get the immediate parent task",
            description = """
            Retrieves the immediate parent task of the specified task in the hierarchy.
            Returns only the direct parent (one level up), not the entire ancestry chain.

            Parent-Child Relationships:
            - If task is TASK: returns its STORY parent
            - If task is STORY: returns its EPIC parent (if exists)
            - If task is EPIC: returns null (Epics are top-level)
            - If task is standalone: returns null (no parent)

            Hierarchy Context:
                Epic (returned if task is Story)
                    Story (returned if task is Task)
                        Task (input taskId)

            What is Retrieved:
            - Complete parent task information
            - All parent task fields (title, status, progress, etc.)
            - Related entities: assignee, category, workspace names
            - Parent task progress and timestamps
            - Returns null if task has no parent

            Use Cases:
            - Building breadcrumb navigation (Home > Epic > Story > Task)
            - Displaying parent context in task details view
            - Hierarchy navigation (move up one level)
            - Validating task hierarchy relationships
            - Parent task reference in UI

            Performance:
            - Single optimized database query
            - Direct parent lookup, no traversal
            - Typical response time: 20 to 50 milliseconds

            Null Response:
            Returns null (with 200 OK status) when:
            - Task is type EPIC (top-level)
            - Task is standalone (no parent assigned)
            - Parent task has been soft-deleted

            Not an Error:
            Null response is valid and expected for top-level or standalone tasks.
            Client should handle null gracefully.

            Use for Breadcrumbs:
            To build full breadcrumb trail, call this endpoint recursively:
            1. Get parent of current task
            2. Get parent of that parent
            3. Continue until null is returned

            Example Response for Task:
            Returns its Story parent:
            {
              "id": 20,
              "title": "User Authentication",
              "taskType": "STORY",
              "parentTaskId": 10,
              "parentTaskTitle": "Security Features"
            }

            Example Response for Story:
            Returns its Epic parent:
            {
              "id": 10,
              "title": "Security Features",
              "taskType": "EPIC",
              "parentTaskId": null,
              "parentTaskTitle": null
            }

            Example Response for Epic:
            Returns null (Epic has no parent):
            null

            Error Handling:
            - 404 Not Found: Task with given ID does not exist
            - 403 Forbidden: User does not have access to task workspace
            - 200 OK with null body: Task has no parent (valid response)

            Example:
            GET /api/v1/tasks/hierarchy/30/parent

            Note:
            This endpoint returns the parent task data. The input parameter is the child task ID.
            """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Parent task retrieved successfully or null if no parent"),
            @ApiResponse(responseCode = "404", description = "Task not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<ApiResult<TaskResponse>> getParentTask(
            @Parameter(description = "ID of the child task to find parent for",
                    example = "30",
                    required = true)
            @PathVariable Long taskId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return executeMethod(
                userDetails.getId(),
                "GET_PARENT_TASK",
                () -> hierarchyService.getParentTask(taskId)
        );
    }

    @LogActivity(ActionType.VIEW)
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @GetMapping("/hierarchy/{taskId}/depth")
    @Operation(summary = "Calculate the hierarchical depth level of a task",
            description = """
            Calculates the depth level of a task within the hierarchy structure by counting
            the number of parent levels above it.

            Depth Level Definition:
            - Depth 0: EPIC (top-level, no parent)
            - Depth 1: STORY (parent is Epic)
            - Depth 2: TASK (parent is Story)
            - Depth 3+: Not allowed in this system

            Calculation Method:
            Traverses upward from the task through parent relationships until reaching
            a task with no parent. The number of parent traversals equals the depth.

            Hierarchy Examples:
            1. Standalone Epic:
               Epic (depth 0)
               Returns: 0

            2. Story under Epic:
               Epic (depth 0)
                   Story (depth 1)
               Returns: 1 for Story

            3. Task under Story under Epic:
               Epic (depth 0)
                   Story (depth 1)
                       Task (depth 2)
               Returns: 2 for Task

            4. Standalone Story:
               Story (depth 0, no parent)
               Returns: 0

            Use Cases:
            - Hierarchy validation during task creation
            - UI indentation and styling (indent by depth level)
            - Tree view visualization depth calculation
            - Enforcing maximum hierarchy depth constraints
            - Breadcrumb generation
            - Access control based on hierarchy level

            Performance:
            - Traverses parent chain (up to 10 iterations max)
            - Typical response time: 20 to 80 milliseconds
            - Single database query per parent level
            - Efficient for standard 3-level hierarchy

            Maximum Depth Safety:
            - System enforces maximum depth of 10 (safety limit)
            - Standard hierarchy is 3 levels (Epic, Story, Task)
            - If traversal exceeds 10 levels, stops and returns 10
            - Prevents infinite loops from circular references

            Standalone Tasks:
            - Standalone Epic: returns 0
            - Standalone Story: returns 0
            - Standalone Task: returns 0
            - Depth is based on actual parent chain, not task type

            Client Usage:
            Can be used to determine appropriate styling:
            - Depth 0: No indentation, bold font
            - Depth 1: Indent 20px, normal font
            - Depth 2: Indent 40px, smaller font

            Return Value:
            Integer representing hierarchical depth (0 to 10):
            - 0: Top-level task (no parent)
            - 1: One parent above
            - 2: Two parents above
            - 10: Maximum depth reached (safety limit)

            Error Handling:
            - 404 Not Found: Task with given ID does not exist
            - 403 Forbidden: User does not have access to task workspace

            Example:
            GET /api/v1/tasks/hierarchy/35/depth

            Example Response:
            2

            Note:
            This is a utility endpoint primarily for UI rendering and validation.
            Most clients can calculate depth client-side by counting parent levels.
            """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Depth calculated successfully"),
            @ApiResponse(responseCode = "404", description = "Task not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<ApiResult<Integer>> getHierarchyDepth(
            @Parameter(description = "ID of the task to calculate depth for",
                    example = "35",
                    required = true)
            @PathVariable Long taskId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return executeMethod(
                userDetails.getId(),
                "GET_HIERARCHY_DEPTH",
                () -> hierarchyService.getHierarchyDepth(taskId)
        );
    }

    @LogActivity(ActionType.VIEW)
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @GetMapping("/hierarchy/{parentTaskId}/nextSortOrder")
    @Operation(summary = "Get next available sort order for adding child tasks under parent",
            description = """
            Calculates the next available sort order value for a new task being added
            under the specified parent task. Ensures consistent ordering and prevents conflicts.

            Sort Order Purpose:
            - Determines display order of tasks in UI
            - Used for manual task reordering
            - Maintains user-defined task sequence
            - Independent per parent (each parent has own sort sequence)

            Calculation Method:
            Returns the maximum existing sort order among current children plus 1.
            Formula: max(existing_sort_orders) + 1

            Sort Order Examples:
            1. Parent has 3 children with sort orders [0, 1, 2]:
               Returns: 3 (next available)

            2. Parent has children with sort orders [0, 5, 10]:
               Returns: 11 (max is 10, so next is 11)

            3. Parent has no children:
               Returns: 0 (first child)

            4. Parent has children with sort orders [5, 2, 8]:
               Returns: 9 (max is 8, so next is 9)

            Use Cases:
            - Task creation forms (pre-calculate sort order)
            - API clients adding tasks programmatically
            - Drag-and-drop task ordering interfaces
            - Bulk task import operations
            - Task duplication with correct ordering

            Typical Workflow:
            1. Client calls this endpoint to get next sort order
            2. Client includes returned value in task creation request
            3. New task is created with correct sort order
            4. Task appears at end of parent's child list in UI

            Parent Types:
            - If parent is EPIC: returns next sort order for new Story
            - If parent is STORY: returns next sort order for new Task
            - Works for any parent with children

            Performance:
            - Single database query (SELECT MAX)
            - Typical response time: 20 to 50 milliseconds
            - Efficient for any number of children

            Alternative Approach:
            Instead of calling this endpoint, clients can omit sortOrder in creation request.
            The system will automatically assign next sort order. This endpoint is useful
            when clients need to know the sort order before creating the task.

            Return Value:
            Integer representing next available sort order:
            - 0: Parent has no children (first child)
            - N+1: Parent has children, max sort order is N

            Thread Safety:
            This endpoint provides the next sort order based on current state.
            If multiple concurrent requests create tasks, sort order conflicts may occur.
            The system handles this gracefully by accepting any integer sort order.

            Gaps in Sort Order:
            Sort orders do not need to be sequential. Gaps are allowed:
            - [0, 1, 2] is valid
            - [0, 5, 10] is also valid
            This allows for manual reordering without updating all siblings.

            Error Handling:
            - 404 Not Found: Parent task with given ID does not exist
            - 403 Forbidden: User does not have access to parent task workspace

            Example:
            GET /api/v1/tasks/hierarchy/20/nextSortOrder

            Example Response:
            3

            Usage Example:
            1. GET /api/v1/tasks/hierarchy/20/nextSortOrder → returns 3
            2. POST /api/v1/tasks/createTask with sortOrder: 3
            3. New task created with sortOrder 3 under parent 20

            Note:
            This is a utility endpoint. Most task creation endpoints automatically assign
            sort order if not provided. Use this endpoint only when you need to know the
            sort order value before creating the task.
            """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Next sort order calculated successfully"),
            @ApiResponse(responseCode = "404", description = "Parent task not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<ApiResult<Integer>> getNextSortOrderForParent(
            @Parameter(description = "ID of the parent task to calculate next sort order for",
                    example = "20",
                    required = true)
            @PathVariable Long parentTaskId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ){
        return executeMethod(
                userDetails.getId(),
                "GET_NEXT_SORT_ORDER_FOR_PARENT",
                () -> hierarchyService.getNextSortOrderForParent(parentTaskId)
        );
    }

}
