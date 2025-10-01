package com.omori.taskmanagement.controller.task;

import com.omori.taskmanagement.annotations.LogActivity;
import com.omori.taskmanagement.dto.common.ApiResult;
import com.omori.taskmanagement.dto.project.task.TaskResponse;
import com.omori.taskmanagement.dto.project.task.creation.*;
import com.omori.taskmanagement.dto.project.task.creation.TaskCreateResponse;
import com.omori.taskmanagement.dto.project.task.update.TaskUpdateRequest;
import com.omori.taskmanagement.model.audit.ActionType;
import com.omori.taskmanagement.model.project.Task;
import com.omori.taskmanagement.security.service.CustomUserDetails;
import com.omori.taskmanagement.service.task.creation.EpicCreationService;
import com.omori.taskmanagement.service.task.creation.StoryCreationService;
import com.omori.taskmanagement.service.task.creation.TaskCreationService;
import com.omori.taskmanagement.service.task.delete.TaskDeletionService;
import com.omori.taskmanagement.service.task.hierarchy.TaskHierarchyService;
import com.omori.taskmanagement.service.task.update.TaskProgressService;
import com.omori.taskmanagement.service.task.update.TaskUpdateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/tasks")
@Slf4j
@Tag(name = "Task Management")
public class TaskCommandController {

    private final TaskCreationService creationService;
    private final StoryCreationService storyCreationService;
    private final EpicCreationService epicCreationService;

    private final TaskUpdateService updateService;
    private final TaskProgressService progressService;

    private final TaskDeletionService deletionService;

    private final TaskHierarchyService hierarchyService;


    @LogActivity(ActionType.CREATE)
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @PostMapping("/create")
    @Operation(summary = "Create a new standalone task",
    description = """
            Creates an independent task that exists without any parent Story or Epic.
            This endpoint creates a bottom-level task in the hierarchy.

                    Task Hierarchy Context: \s
                        (No parent) \s
                            └──Standalone Task (This endpoint creates this) \s
                                    └───Subtasks (can be added later) \s

            What Gets Created:
            - Task with type TASK (automatically set)
            - Associated with specified workspace and category
            - Can be assigned to a user immediately or left unassigned
            - Initial progress 0 percent, status PENDING (unless specified otherwise)
            - Timestamps (createdAt, updatedAt) set automatically

            Use Cases:
            - Quick task creation without organizational structure
            - Personal tasks that do not belong to any project
            - Tasks for small projects without Epic or Story organization
            - Temporary or experimental tasks

            Field Behavior:
            - workspaceId: Required, task must belong to a workspace
            - categoryId: Optional, for task categorization
            - assignedToId: Optional, can assign immediately or later
            - dueDate: Optional, if not set task has no deadline
            - priority: Optional, defaults to MEDIUM if not specified
            - progress: Optional, defaults to 0 if not specified

            Validation Rules:
            - User must have access to the specified workspace
            - Assigned user (if specified) must exist and have workspace access
            - Category (if specified) must exist and belong to the workspace
            - Start date cannot be after due date (if both provided)

            Response:
            Returns TaskCreateResponse with generated task ID and UUID, all field values including defaults,
            timestamps, and related entity information (workspace, category, assignee).

            Transaction Safety:
            Entire operation is transactional. Rollback occurs if any validation or persistence fails.
            """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Task created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data or validation failed"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions to create task"),
            @ApiResponse(responseCode = "404", description = "Workspace or related entity not found")
    })
    public ResponseEntity<ApiResult<TaskCreateResponse>> createStandaloneTask(
            @Valid @RequestBody StandaloneTaskRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Task task = creationService.createStandaloneTask(userDetails.getId(), request);
        TaskCreateResponse response = TaskCreateResponse.from(task);
        return ResponseEntity.status(201).body(ApiResult.success(response));
    }

    @LogActivity(ActionType.CREATE)
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @PostMapping("/createTask")
    @Operation(summary = "Create a new task under an existing Story",
            description = """
            Creates a child task under an existing Story (hierarchy level 2).
            This establishes a parent-child relationship in the task hierarchy.

                    Task Hierarchy Context:
                        Epic (Level 0) \s
                            └──Story (Level 1) (Parent must be this type) \s
                                └──Task (Level 2) (This endpoint creates this) \s
                                    Subtask

            What Gets Created:
            - Task with type TASK (automatically set)
            - Parent-child relationship: parentTask equals specified Story
            - Inherits workspace from parent Story (cannot be different)
            - Independent category, assignee, dates, and other fields
            - Sort order assigned automatically for task ordering within Story

            Parent Story Impact:
            - Story child task count increases
            - Story progress may be recalculated (if auto-calculation enabled)
            - Story remains unchanged otherwise

            Use Cases:
            - Breaking down user stories into actionable tasks
            - Organizing work within a Story during sprint planning
            - Creating implementation tasks for a feature Story
            - Distributing Story work among team members

            Validation Rules:
            - parentId: Required in request, must reference existing Story
            - Parent task must be of type STORY (Epic or Task not allowed)
            - User must have access to parent Story workspace
            - Parent Story must not be soft-deleted
            - Parent Story must be in same workspace (enforced automatically)

            Field Behavior:
            - workspaceId: Inherited from parent Story (ignored if provided)
            - parentId: Required, the Story ID this task belongs to
            - categoryId: Optional, can differ from parent Story category
            - assignedToId: Optional, tasks can have different assignees
            - All other fields: Independent of parent Story values

            Progress Propagation:
            When this task progress changes (via subtasks), it automatically triggers
            parent Story progress recalculation using weighted formula:
            Story progress equals 50 percent own subtasks plus 50 percent average of child tasks

            Response:
            Returns TaskCreateResponse with task details including parentTaskId and parentTaskUuid,
            inherited workspace information, and sort order within parent Story.

            Transaction Safety:
            Entire operation is transactional with parent validation. Rollback if parent Story
            does not exist or validation fails.
            """)
    public ResponseEntity<ApiResult<TaskCreateResponse>> createTaskUnderStory(
        @Valid @RequestBody TaskCreateRequest request,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ){
        Task task = creationService.createTaskUnderStory(userDetails.getId(), Task.TaskType.TASK, request);
        TaskCreateResponse response = TaskCreateResponse.from(task);
        return ResponseEntity.status(201).body(ApiResult.success(response));
    }

    @LogActivity(ActionType.CREATE)
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @PostMapping("/createStory")
    @Operation(summary = "Create a new standalone Story task",
            description = """
            Creates an independent Story task at hierarchy level 1 without a parent Epic.
            Stories are containers for organizing related tasks and represent features or user stories.

                    Task Hierarchy Context: \s
                        (No parent) \s
                            └──Standalone Story (This endpoint creates this) \s
                                └──Tasks (can be added later) \s
                                        Subtasks

            Story Characteristics:
            - Task Type: Automatically set to STORY
            - Hierarchy Position: Level 1 (no parent)
            - Children: Can contain TASK type children
            - Purpose: Feature organization, user story representation
            - Progress: Can be based on own subtasks and/or child tasks

            What Gets Created:
            - Story task with type STORY (automatically set)
            - No parent relationship (standalone)
            - Associated with specified workspace
            - Can have category and assignee
            - Initial progress 0 percent
            - Can have subtasks and child tasks added later

            Use Cases:
            - Creating user stories without Epic organization
            - Feature stories for small projects
            - Standalone features that do not fit into existing Epics
            - Rapid prototyping and experimentation
            - Projects that do not require Epic-level organization

            Field Behavior:
            - parentId: Ignored if provided (Stories created here are always standalone)
            - taskType: Automatically set to STORY regardless of request value
            - workspaceId: Required, Story must belong to a workspace
            - categoryId: Optional, for Story categorization
            - assignedToId: Optional, Story owner or lead
            - progress: Defaults to 0, calculated from subtasks and/or child tasks

            Validation Rules:
            - User must have workspace access
            - Workspace must exist and be active
            - Assigned user (if specified) must have workspace access
            - Category (if specified) must belong to the workspace

            Progress Calculation:
            Standalone Story progress can be calculated from:
            1. Own subtasks only (if no child tasks)
            2. Child tasks only (if no own subtasks)
            3. Weighted: 50 percent own subtasks plus 50 percent average child tasks (if both exist)

            Migration Path:
            A standalone Story can later be moved under an Epic by using task update endpoint
            to set parentId to Epic ID. Hierarchy validation ensures Epic can accept Story children.

            Response:
            Returns TaskCreateResponse with Story ID and UUID, taskType STORY, parentId null (no parent),
            and workspace and category information.

            Transaction Safety:
            Entire operation is transactional. Rollback on validation or persistence failure.
            """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Task created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data or validation failed"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions to create task"),
            @ApiResponse(responseCode = "404", description = "Workspace or related entity not found")
    })
    public ResponseEntity<ApiResult<TaskCreateResponse>> createStoryTask(
            @Valid @RequestBody StoryCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Task task = storyCreationService.createStoryTask(userDetails.getId(), request);
        TaskCreateResponse response = TaskCreateResponse.from(task);
        return ResponseEntity.status(201).body(ApiResult.success(response));
    }

    @LogActivity(ActionType.CREATE)
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @PostMapping("/createStoryUnderEpic")
    @Operation(summary = "Create a new Story under an existing Epic",
            description = """
            Creates a child Story task under an existing Epic, establishing Epic to Story hierarchy.
            This is the standard way to organize features and user stories within large initiatives.

                    Task Hierarchy Context: \s
                        Epic (Level 0) (Parent must be this type) \s
                            └──Story (Level 1) (This endpoint creates this) \s
                                └──Tasks (can be added later) \s
                                        Subtasks \s

            What Gets Created:
            - Story with type STORY (automatically set)
            - Parent-child relationship: parentTask equals specified Epic
            - Inherits workspace from parent Epic (cannot be different)
            - Independent category, assignee, dates, priority
            - Sort order assigned for Story ordering within Epic
            - Can immediately have child tasks or be populated later

            Parent Epic Impact:
            - Epic child Story count increases
            - Epic progress will be recalculated when Story progress changes
            - Epic remains active and unchanged otherwise

            Use Cases:
            - Adding user stories to Epics during sprint planning
            - Breaking down large Epics into feature components
            - Organizing related stories under thematic Epics
            - Creating feature backlog within product initiatives
            - Agile and Scrum story organization within Epics

            Validation Rules:
            - parentId: Required in request, must reference existing Epic
            - Parent task must be of type EPIC (Story or Task not allowed)
            - Parent Epic must not be soft-deleted
            - User must have access to Epic workspace
            - Parent Epic must allow Story children (business rule validation)
            - Parent Epic and Story must be in same workspace (enforced automatically)

            Field Behavior:
            - parentId: Required, the Epic ID this Story belongs to
            - taskType: Automatically set to STORY
            - workspaceId: Inherited from parent Epic (ignored if provided)
            - categoryId: Optional, can differ from Epic category
            - assignedToId: Optional, Story lead or owner
            - All date and priority fields: Independent of parent Epic

            Progress Propagation:
            When this Story progress changes, it automatically triggers parent Epic
            progress recalculation using weighted formula:
            Epic progress equals 50 percent own subtasks plus 50 percent average of child Stories

            Hierarchy Flow Example:
            1. Create Epic "User Authentication System"
            2. Create Story "Login Page" under Epic (this endpoint)
            3. Create Story "Password Reset" under Epic (this endpoint)
            4. Create Tasks under each Story
            5. As Tasks complete, Stories auto-update, then Epic auto-updates

            Response:
            Returns TaskCreateResponse with Story details including parentTaskId and parentTaskUuid,
            taskType STORY, inherited workspace information, and sort order within parent Epic.

            Transaction Safety:
            Entire operation is transactional with hierarchy validation. Rollback if parent Epic
            validation fails. Maintains hierarchy consistency.
            """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Task created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data or validation failed"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions to create task"),
            @ApiResponse(responseCode = "404", description = "Workspace or related entity not found")
    })
    public ResponseEntity<ApiResult<TaskCreateResponse>> createStoryUnderEpic(
            @Valid @RequestBody StoryCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Task task = storyCreationService.createStoryUnderEpic(userDetails.getId(), Task.TaskType.STORY, request);
        TaskCreateResponse response = TaskCreateResponse.from(task);
        return ResponseEntity.status(201).body(ApiResult.success(response));
    }

    @LogActivity(ActionType.CREATE)
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @PostMapping("/createEpic")
    @Operation(summary = "Create a standalone Epic task",
            description = """
            Creates a standalone Epic at the top level of the task hierarchy. \s
            Epics represent large initiatives, features, or project phases that span multiple sprints
            and contain multiple Stories. \s

                    Task Hierarchy Context:
                        Epic (Level 0) (This endpoint creates this)
                            └──Stories (can be added later)
                                └──Tasks
                                        Subtasks

            Epic Characteristics:
            - Task Type: Automatically set to EPIC
            - Hierarchy Position: Top level (no parent ever)
            - Children: Can contain STORY type children only
            - Scope: Large initiatives spanning weeks or months
            - Purpose: High-level feature or initiative organization
            - Visibility: Typically shown in roadmaps and release planning

            What Gets Created:
            - Epic task with type EPIC (automatically set)
            - No parent relationship (Epics are always top-level)
            - Associated with specified workspace
            - Initial progress 0 percent
            - Can have child Stories added immediately or later
            - Can have own subtasks for Epic-level work items

            Epic Creation Process:
            1. Validates Epic creation request and user permissions
            2. Creates Epic with TaskType EPIC automatically assigned
            3. Sets Epic as standalone (ignores any parentId in request)
            4. Assigns default values: progress 0, sortOrder (auto)
            5. Establishes workspace and category relationships
            6. Sets timestamps (createdAt, updatedAt)
            7. Generates unique UUID for external references

            Use Cases:
            - Creating major product initiatives (example: Mobile App Launch)
            - Organizing features for product releases (example: Q4 2025 Release)
            - Planning large-scale development efforts
            - Grouping related user stories for sprint planning
            - Portfolio and program management and roadmap planning
            - Multi-sprint features and cross-team initiatives

            Field Behavior:
            - parentId: Always ignored, Epics cannot have parents
            - taskType: Automatically set to EPIC regardless of request
            - workspaceId: Required, Epic must belong to a workspace
            - categoryId: Optional, for Epic categorization
            - assignedToId: Optional, Epic owner or lead (typically PM or Tech Lead)
            - progress: Defaults to 0, calculated from subtasks and child Stories
            - dueDate: Optional, Epic end date or target release date
            - priority: Optional, Epic priority (HIGH for important initiatives)

            Validation Rules:
            - User must have workspace access and CREATE_EPIC permission
            - Workspace must exist and be active
            - Assigned user (if specified) must have workspace access
            - Category (if specified) must belong to the workspace
            - Date validations: startDate cannot be after dueDate

            Progress Calculation:
            Epic progress is calculated using weighted formula:
            - If Epic has both subtasks AND child Stories:
                Epic progress equals 50 percent own subtasks plus 50 percent average of child Stories
            - If only subtasks: progress equals subtask completion percentage
            - If only Stories: progress equals average Story progress
            - If neither: progress equals 0

            Epic vs Story vs Task:
            - Epic: Large initiative, contains Stories, top-level
            - Story: Feature or user story, contains Tasks, under Epic
            - Task: Implementation work, contains Subtasks, under Story

            Response:
            Returns TaskCreateResponse with Epic ID and UUID, taskType EPIC, parentId null (no parent),
            workspace and category information, and all field values including defaults. \s

            Transaction Safety:
            Entire operation is transactional. Rollback on validation or persistence failure. \s
            Maintains data consistency. \s

            Performance Note:
            Epic creation is lightweight. The performance impact comes from adding many child Stories
            and their Tasks, not Epic creation itself.
            """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Task created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data or validation failed"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions to create task"),
            @ApiResponse(responseCode = "404", description = "Workspace or related entity not found")
    })
    public ResponseEntity<ApiResult<TaskCreateResponse>> createEpicTask(
            @Valid @RequestBody EpicCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Task task = epicCreationService.createEpicTask(userDetails.getId(),request);
        TaskCreateResponse response = TaskCreateResponse.from(task);
        return ResponseEntity.status(201).body(ApiResult.success(response));
    }

    @LogActivity(ActionType.CREATE)
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @PostMapping("/createEpicWithInitStories")
    @Operation(summary = "Create an Epic with initial Stories in a single transaction",
            description = """
            Creates an Epic and immediately populates it with initial child Stories atomically. \s
            All operations succeed or fail together. If any Story creation fails,
            the entire operation rolls back including the Epic creation. \s

            Task Hierarchy Created:
                Epic (Level 0) (Created first)
                    Story 1 (Level 1) (Created from initialStories at index 0)
                    Story 2 (Level 1) (Created from initialStories at index 1)
                    Story N (Level 1) (Created from initialStories at index n minus 1)

            What Gets Created:
            1. Epic task (same as createEpicTask)
                - Type EPIC
                - No parent
                - Associated with workspace
                - Initial progress 0 percent

            2. Initial Stories (from request.initialStories)
                - Each Story is created as child of the Epic
                - Type STORY (automatically set)
                - ParentId newly created Epic ID
                - Inherit Epic workspace
                - Sort orders assigned automatically

            Initial Stories Configuration:
            The initialStories field in the request is a list of Story creation requests.
            Each Story in the list will:
            - Have parentId automatically set to the Epic ID
            - Have taskType automatically set to STORY
            - Inherit the Epic workspace (workspaceId ignored if provided)
            - Can have independent category, assignee, dates, priority
            - Be assigned sequential sort orders for ordering

            Empty Initial Stories:
            - If initialStories is null or empty array: only Epic is created
            - No error is thrown for empty initialStories
            - Behaves exactly like createEpicTask if no initial Stories

            Use Cases:
            - Sprint planning: Create Epic with planned Stories in one call
            - Project templates: Pre-populate Epic with standard Stories
            - Bulk import: Migrate Epic plus Stories from external systems
            - API efficiency: Reduce round trips for Epic plus Stories creation
            - Atomic organization: Ensure Epic and Stories created together

            Validation Rules:
            Epic Level:
            - Standard Epic validation (workspace access, category, etc.)

            Story Level (each Story in initialStories):
            - Story title cannot be null or empty
            - Assigned user (if specified) must have workspace access
            - Category (if specified) must belong to Epic workspace
            - Date validations applied per Story

            Atomic Behavior:
            - Entire operation runs in a single database transaction
            - If Epic creation fails, nothing is created
            - If any Story creation fails, entire operation rolls back
            - All Stories succeed together or none are created
            - Database remains consistent even if operation fails mid-way

            Progress Behavior:
            - Epic initial progress 0 percent (no Story progress yet)
            - As Stories are later populated with Tasks and progress,
                Epic progress auto-updates via hierarchy propagation

            Sort Order:
            - Stories are assigned sort orders based on array position
            - First Story: sortOrder 1
            - Second Story: sortOrder 2
            - And so on
            - This maintains the order specified in initialStories array

            Response:
            Returns TaskCreateResponse for the Epic with Epic details (ID, UUID, all fields),
            child Stories information included, Story IDs and UUIDs for reference,
            and complete hierarchy structure.

            Performance Considerations:
            - Creating Epic plus 5 Stories: approximately 200 to 500 milliseconds depending on database
            - Creating Epic plus 20 Stories: approximately 500 milliseconds to 1 second
            - For bulk operations (50 plus Stories), consider separate API calls
            - Database insert performance scales linearly with Story count

            Error Handling:
            - Epic validation errors: 400 Bad Request
            - Workspace not found: 404 Not Found
            - Any Story validation error: 400 Bad Request (entire rollback)
            - Database errors: 500 Internal Server Error (entire rollback)

            Transaction Safety:
            - Full ACID compliance
            - Rollback on any failure
            - No partial Epic plus Story combinations in database
            - Maintains referential integrity
            """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Task created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data or validation failed"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions to create task"),
            @ApiResponse(responseCode = "404", description = "Workspace or related entity not found")
    })
    public ResponseEntity<ApiResult<TaskCreateResponse>> createEpicWithInitialStories(
            @Valid @RequestBody EpicWithStoriesRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Task task = epicCreationService.createEpicWithInitialStories(userDetails.getId(), request);
        TaskCreateResponse response = TaskCreateResponse.from(task);
        return ResponseEntity.status(201).body(ApiResult.success(response));
    }

    @LogActivity(ActionType.UPDATE)
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @PatchMapping("/update/{taskId}")
    @Operation(summary = "Update task fields using partial update strategy (PATCH)",
            description = """
                    Updates an existing task with partial data. Only fields provided in the request are modified.
                    This is a PATCH operation following REST semantics: unspecified fields remain unchanged.

                    HTTP Method Semantics:
                    - PATCH: Partial update (only specified fields changed)
                    - PUT would be full replacement (not used here)
                    - This endpoint uses PATCH semantics exclusively

                    Update Process:
                    1. Load task with all relations (assignee, category, workspace, parent)
                    2. Validate user access (must be owner or assignee)
                    3. Apply partial updates:
                        a. Update basic fields (only non-null request fields)
                        b. Update relationships (category, assignee, workspace if specified)
                        c. Apply business logic (status consistency, progress sync)
                    4. Save updated task to database
                    5. Update cache (after successful transaction commit)
                    6. Return complete updated TaskResponse

                    Updatable Fields:
                    Basic Fields (applied if non-null in request):
                    - title: Task title or name
                    - description: Detailed description
                    - dueDate: Deadline timestamp
                    - startDate: Start timestamp
                    - priority: Task priority (LOW, MEDIUM, HIGH, CRITICAL)
                    - status: Task status (PENDING, IN_PROGRESS, COMPLETED, etc.)
                    - estimatedHours: Estimated effort
                    - actualHours: Actual time spent (manual input)
                    - progress: Progress percentage (0 to 100)
                    - sortOrder: Display order
                    - isRecurring: Recurrence flag
                    - recurrencePattern: Cron pattern for recurring tasks
                    - metadata: JSON metadata field

                    Relationship Fields:
                    - categoryId: Change task category
                    - assignedToId: Reassign task
                    - workspaceId: Move to different workspace (with validation)

                    Business Logic Automatically Applied:
                    1. Status-Progress Consistency:
                        - If status to COMPLETED and progress less than 100: progress set to 100
                        - If status to IN_PROGRESS and progress equals 0: progress set to 10
                        - If status to PENDING and progress greater than 0: status changed to IN_PROGRESS

                    2. Completion Date Management:
                        - If status to COMPLETED and completedAt is null: completedAt equals now
                        - If status to not COMPLETED: completedAt equals null

                    3. Timestamp Management:
                        - updatedAt automatically set to current timestamp
                        - createdAt never changes

                    Access Control:
                    User must satisfy ONE of these conditions:
                    - User is the task owner (createdBy equals userId)
                    - User is assigned to the task (assignedTo equals userId)
                    - User has ADMIN role

                    If access denied: TaskAccessDeniedException (403 Forbidden)

                    Validation Rules:
                    - progress: Must be between 0 and 100
                    - startDate and dueDate: startDate cannot be after dueDate
                    - categoryId: Category must exist and belong to workspace
                    - assignedToId: User must exist and have workspace access
                    - workspaceId: Workspace must exist and user must have access

                    Cache Management:
                    - Cache updated AFTER transaction commits (prevents dirty cache)
                    - If transaction rolls back, no cache update occurs
                    - Two cache keys updated: by task ID and by task UUID
                    - List caches (all tasks) are evicted to maintain consistency

                    Progress Propagation:
                    This endpoint does NOT trigger automatic progress propagation to parents.
                    Progress updates here are manual or direct updates.
                    For automatic hierarchy propagation, see:
                    - /progress/{taskId}/recalculate (manual trigger)
                    - Subtask completion events (automatic trigger)

                    Use Cases:
                    - Updating task status during work
                    - Manual progress updates
                    - Reassigning tasks to different users
                    - Changing priorities or deadlines
                    - Updating descriptions or metadata
                    - Moving tasks between categories

                    Response:
                    Returns complete TaskResponse with ALL fields (not just updated ones):
                    - All task fields (updated and unchanged)
                    - Related entity details (assignee, category, workspace)
                    - Updated timestamps
                    - Parent and child relationship info

                    Performance:
                    - Single database query to load task
                    - Single database update to save changes
                    - Cache update is async (does not block response)
                    - Typical response time: 50 to 200 milliseconds

                    Error Handling:
                    - 400 Bad Request: Validation failures (progress greater than 100, dates invalid)
                    - 403 Forbidden: User does not have access to task
                    - 404 Not Found: Task not found, or category, user, workspace not found
                    - 500 Internal Server Error: Database or cache errors

                    Transaction Safety:
                    - Entire operation is transactional
                    - Cache update only after successful commit
                    - Rollback on any validation or persistence failure

                    Important Notes:
                    - This is PATCH, not PUT. Only send fields you want to change
                    - Always returns full task object (for client state refresh)
                    - Parent task, taskType, and UUID cannot be changed via this endpoint
                    - For hierarchy changes, use dedicated hierarchy management endpoints
                    """)
    public ResponseEntity<ApiResult<TaskResponse>> updateTask(
            @Parameter(description = "ID of the task to update",
                    example = "42",
                    required = true)
            @PathVariable Long taskId,
            @Valid @RequestBody TaskUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        TaskResponse task = updateService.updateTask(taskId, userDetails.getId(), request);
        return ResponseEntity.ok(ApiResult.success(task));
    }

    @LogActivity(ActionType.UPDATE)
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @PatchMapping("/progress/{taskId}/recalculate")
    @Operation(summary = "Recalculate and update task progress with automatic hierarchy propagation",
            description = """
                Recalculates a task's progress based on subtask completion, updates the database,
                and automatically propagates progress changes up the task hierarchy. \s
                This is a WRITE operation that triggers cascading updates. \s
                
                Operation Flow:
                1. Fetches task with relations from database
                2. Counts completed subtasks vs total subtasks
                3. Calculates progress percentage: (completed / total) × 100
                4. Updates task.progress field in database
                5. Updates task.updatedAt timestamp
                6. Automatically triggers parent updates (cascade)
                
                Automatic Cascade Behavior:
                - If task has STORY parent → triggers propagateProgressToParent()
                    • Story progress = 50% own subtasks + 50% average of child tasks
                - If task has EPIC parent → triggers updateHierarchyProgress()
                    • Epic progress = 50% own subtasks + 50% average of child stories
                - Cascade continues up the hierarchy until top-level is reached
                
                Transaction Safety:
                - Entire operation runs in a single transaction (@Transactional)
                - If ANY step fails (task update, parent update, etc.), ALL changes rollback
                - Database remains consistent even if cascade fails partway through
                
                When to Use:
                - Manual progress synchronization when data becomes inconsistent
                - After bulk subtask operations (import, batch complete, etc.)
                - Fixing progress values after data migration
                - Admin tools for data maintenance
                - API integrations that bypass automatic event triggers
                
                Important Notes:
                - Normal subtask completion triggers this AUTOMATICALLY via events
                - Manual invocation is typically only needed for edge cases or maintenance
                - Performance: O(n) where n = depth of hierarchy above this task
                
                Example Scenarios:
                - Task has 5 subtasks, 3 completed → sets progress to 60%
                    → If parent is Story → Story recalculates its progress too
                    → If Story has Epic → Epic recalculates its progress too
                
                Error Handling:
                - Throws TaskNotFoundException if taskId doesn't exist
                - Throws TaskBusinessException if database update fails
                - All errors cause complete transaction rollback
                """)
    public ResponseEntity<ApiResult<Void>> recalculateTaskProgress(
            @Parameter(description = "ID of the task to recalculate",
            example = "42",
            required = true)
            @PathVariable Long taskId) {
        progressService.updateProgressFromSubtasks(taskId);
        return ResponseEntity.noContent().build();
    }

    @LogActivity(ActionType.UPDATE)
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @PatchMapping("/progress/epics/{epicId}/refresh")
    @Operation(summary = "Recalculate entire Epic hierarchy progress from top-down",
            description = """
                Performs a comprehensive progress recalculation for an Epic and all its child Stories.
                This is a WRITE operation that updates the Epic's progress using a weighted formula
                combining the Epic's own subtask completion with average progress of child Stories.
                
                Operation Flow:
                1. Validates that specified task is of type EPIC (throws exception otherwise)
                2. Calculates Epic's own progress from direct subtasks
                3. Fetches all child STORY tasks under this Epic
                4. Calculates average progress of all child Stories
                5. Applies weighted formula: (own_progress × 50%) + (children_avg × 50%)
                6. Updates Epic's progress and updatedAt timestamp in database
                
                        Weighted Progress Formula:
                            epic_progress = (subtask_completion × 0.5) + (avg_story_progress × 0.5)
                
                Example Calculation:
                - Epic has 4 subtasks, 2 completed = 50% own progress
                - Epic has 3 Stories with progress: 100%, 80%, 60% = 80% avg
                - Final Epic progress = (50 × 0.5) + (80 × 0.5) = 65%
                
                Edge Cases:
                - Epic with no Stories → progress = 100% own subtask completion
                - Epic with no subtasks → progress = 100% average of child Stories
                - Epic with neither → progress = 0%
                
                        Hierarchy Context:
                            Epic (Level 0) ← This method updates this level \s
                                └── Story (Level 1) \s
                                    └── Task (Level 2) \s
                                        └── Subtasks \s
                
                When to Use:
                - Periodic data consistency checks (scheduled jobs)
                - After bulk Story operations or imports
                - Admin maintenance tools for fixing progress inconsistencies
                - Data migration or system recovery operations
                - When Epic progress appears incorrect due to missed events
                
                Performance Considerations:
                - Queries: 1 Epic fetch + 1 query for all child Stories + 1 subtask count
                - For Epics with many Stories (>50), this may take several seconds
                - Not suitable for high-frequency API calls
                - Consider running asynchronously for large Epics
                - No recursion into Story/Task levels (only Epic + immediate Stories)
                
                Transaction Safety:
                - Entire operation is transactional
                - Rollback occurs if Epic update or any query fails
                
                Validation:
                - Throws TaskNotFoundException if epicId doesn't exist
                - Throws TaskValidationException if task is not type EPIC
                - Use with EPIC tasks only - will reject STORY or TASK types
                
                Comparison with Other Methods:
                - calculateTaskProgress() → READ-ONLY, no database updates
                - recalculateTaskProgress() → Updates ANY task + cascades upward
                - refreshEpicProgress() → Updates EPIC only using weighted formula
                
                Typical Use Case:
                You have an Epic "User Authentication System" with 5 Stories. \s
                After completing multiple Tasks across different Stories, you notice
                the Epic progress is stale. Call this endpoint to refresh the Epic's
                progress based on current Story completion percentages.
                """)
    public ResponseEntity<ApiResult<Void>> refreshEpicProgress(
            @Parameter(description = "ID of the Epic to refresh progress",
            example = "40",
            required = true)
            @PathVariable Long epicId) {
        progressService.updateHierarchyProgress(epicId);
        return ResponseEntity.noContent().build();
    }

    @LogActivity(ActionType.UPDATE)
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @PatchMapping("/hierarchy/{taskId}/move/{parentId}")
    @Operation(summary = "Move a task to a new parent while maintaining hierarchy rules",
            description = """
            Moves a task to a new parent task while validating and maintaining hierarchy constraints.
            This operation updates parent-child relationships and recalculates progress for affected tasks.

            Hierarchy Rules Enforced:
            - STORY tasks can only be moved under EPIC parents
            - TASK tasks can only be moved under STORY parents
            - EPIC tasks cannot be moved (always top-level)
            - Circular references are prevented
            - Maximum hierarchy depth is enforced

            Move Operation Process:
            1. Validate task and new parent exist
            2. Validate hierarchy rules (type compatibility)
            3. Prevent circular references (task cannot be moved under its own descendant)
            4. Check maximum depth constraint
            5. Update task parent relationship
            6. Assign new sort order under new parent
            7. Update timestamps
            8. Recalculate progress for old parent
            9. Recalculate progress for new parent

            Valid Move Scenarios:
            - Story from one Epic to another Epic
            - Task from one Story to another Story
            - Standalone Story to under an Epic
            - Task from one Story to another Story in different Epic

            Invalid Move Scenarios:
            - Moving Epic (not allowed)
            - Moving Story under Story or Task
            - Moving Task under Epic or Task
            - Moving task under its own descendant (circular reference)
            - Moving to parent that would exceed max depth

            Side Effects:
            - Task parent relationship updated
            - Task sort order changed (becomes last child of new parent)
            - Old parent progress recalculated
            - New parent progress recalculated
            - Task updatedAt timestamp updated

            Progress Recalculation:
            After move, both old and new parents have their progress recalculated:
            - If old parent is STORY: propagates to parent Epic
            - If new parent is STORY: propagates to parent Epic
            - If old parent is EPIC: recalculates Epic progress
            - If new parent is EPIC: recalculates Epic progress

            Circular Reference Prevention:
            System prevents these invalid moves:
            - Moving Epic 1 under its child Story 2
            - Moving Story 2 under its child Task 3
            - Any move that creates a loop in the hierarchy

            Maximum Depth Constraint:
            - Maximum allowed depth is 3 levels
            - Epic (depth 0) > Story (depth 1) > Task (depth 2)
            - Move rejected if it would create deeper hierarchy

            Use Cases:
            - Reorganizing tasks during sprint planning
            - Moving Stories between Epics
            - Reassigning Tasks to different Stories
            - Project restructuring and refactoring

            Performance:
            - Single database transaction
            - Progress recalculation may add 50-200ms
            - Typical response time: 100 to 300 milliseconds

            Transaction Safety:
            - Entire operation is transactional
            - All changes rollback if any step fails
            - Database consistency maintained

            Validation Errors:
            - EPIC cannot be moved
            - STORY can only move to EPIC parent
            - TASK must have STORY parent
            - Circular reference detected
            - Maximum depth exceeded

            Error Handling:
            - 400 Bad Request: Hierarchy rule violation, circular reference, or depth exceeded
            - 404 Not Found: Task or parent task does not exist
            - 403 Forbidden: User does not have access to task or parent workspace

            Example Move Story to Different Epic:
            PATCH /api/v1/tasks/hierarchy/25/move/15
            Moves Story 25 from its current Epic to Epic 15

            Example Move Task to Different Story:
            PATCH /api/v1/tasks/hierarchy/35/move/22
            Moves Task 35 from its current Story to Story 22

            Response:
            Returns 204 No Content on success. No response body.

            Important Notes:
            - This is a PATCH operation (partial update)
            - Task properties (title, status, etc.) remain unchanged
            - Only parent relationship and sort order are modified
            - Progress recalculation happens automatically
            - Cannot be undone easily (consider implementing move history)
            """
    )
    public ResponseEntity<ApiResult<Void>> moveTaskToParent(
            @Parameter(description = "ID of the task to move",
            example = "38",
            required = true)
            @PathVariable Long taskId,
            @Parameter(description = "ID of the new parent task",
            example = "32",
            required = true)
            @PathVariable Long parentId
    ) {
        hierarchyService.moveTaskToParent(taskId, parentId);
        return ResponseEntity.noContent().build();
    }

}
