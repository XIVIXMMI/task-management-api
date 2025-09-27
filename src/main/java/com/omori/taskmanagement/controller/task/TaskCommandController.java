package com.omori.taskmanagement.controller.task;

import com.omori.taskmanagement.annotations.LogActivity;
import com.omori.taskmanagement.dto.common.ApiResult;
import com.omori.taskmanagement.dto.project.task.creation.*;
import com.omori.taskmanagement.dto.project.task.TaskCreateResponse;
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
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
            Creates a new standalone task that is not associated with any parent story. \s
            This method creates an independent task that exists at the bottom level of the task hierarchy.\s
            The task type is automatically determined from the request or defaults to the standard TASK type. \s
            \s""")
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
    @Operation(summary = "Create a new task under story level",
            description = """
            Creates a new task under an existing story (task level 2) with the specified task type. \s
            This method creates a child task that belongs to a parent story, establishing a hierarchical relationship.\s
            The task type must be compatible with story-child relationships (typically TASK or SUBTASK types).
            \s""")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Task created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data or validation failed"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions to create task"),
            @ApiResponse(responseCode = "404", description = "Workspace or related entity not found")
    })
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
    @Operation(summary = "Create a new standalone story task",
            description = """
            Creates a new standalone story task that is not associated with any parent Epic. \s
            This method creates an independent story that exists at the second level of the task hierarchy.\s
            The story can later have child tasks created under it. \s
            Any parentId provided in the request will be ignored to ensure the story remains standalone. \s
            Use Cases:
            - Creating user stories that don't belong to a specific Epic
            - Creating feature stories for small projects that don't require Epic organization
            - Creating temporary or experimental stories for rapid prototyping\s
            \s""")
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
    @Operation(summary = "Create a story task have parent task",
            description = """
            Creates a new story task under an existing Epic with hierarchical relationships. \s
            This method creates a child story that belongs to a parent Epic, establishing a hierarchical relationship. \s
            The Epic must exist and be accessible to the user. \s
            The story will be positioned within the Epic's child collection with appropriate sort ordering. \s
            Use Cases:
            - Adding user stories to an existing Epic during sprint planning
            - Breaking down large Epics into manageable Story components
            - Organizing related features under a common Epic theme
            Validation Rules:
            - The parentId in the request must not be null
            - The parent task must exist and be of type EPIC
            - The user must have access to both the workspace and the parent Epic
            - The Epic must be able to contain Story-type children (business rule validation)
            \s""")
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
        Task task = storyCreationService.createStoryUnderEpic(userDetails.getId(), Task.TaskType.EPIC, request);
        TaskCreateResponse response = TaskCreateResponse.from(task);
        return ResponseEntity.status(201).body(ApiResult.success(response));
    }

    @LogActivity(ActionType.CREATE)
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @PostMapping("/createEpic")
    @Operation(summary = "Create a story task have parent task",
            description = """
            Creates a new standalone Epic task at the top level of the task hierarchy. \s
            This method creates an independent Epic that serves as the highest level container in the task management hierarchy. \s
            Epics are used to group related Stories and represent large initiatives, features, or project phases that span multiple sprints. \s
            Epic Characteristics:
            - Task Type: Automatically set to TaskType.EPIC
            - Hierarchy Position: Top level (no parent task)
            - Children: Can contain Story tasks
            - Purpose: High-level feature or initiative organization
            Task Hierarchy Context:
            Epic (Level 0) ← This method creates these \s
                └── Story (Level 1) \s
                    └── Task (Level 2) \s
            
            Epic Creation Process:
            1. Validates the Epic creation request and user permissions
            2. Creates the Epic with TaskType.EPIC automatically assigned
            3. Sets Epic as standalone (no parent) by ignoring any parentId in request
            4. Assigns default values for progress, sort order, and timestamps
            5. Establishes workspace and category relationships
            \s
            Use Cases:
            - Creating major project initiatives (e.g., "User Authentication System")
            - Organizing features for product releases
            - Planning large-scale development efforts
            - Grouping related user stories for sprint planning
            Field Behavior:
            - parentId: Ignored even if provided (Epics are always top-level)
            - taskType: Automatically set to EPIC regardless of request value
            - progress: Defaults to 0, will be calculated from child Stories
            - sortOrder: Assigned automatically for Epic ordering
            \s""")
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
    @Operation(summary = "Create a story task have parent task",
            description = """
            Creates an Epic task with initial story tasks in a single transaction. \s
            This method creates an Epic and immediately adds the provided initial stories as child tasks under the Epic. \s
            All operations are performed atomically - if any story creation fails, the entire operation is rolled back. \s
            Initial Stories Source: The initial stories are obtained from request.getInitialStories(). \s
            If this field is null or empty, only the Epic will be created without any child stories. \s
            Story Configuration: Each story in the initial list will:
            - Have its parentId automatically set to the created Epic's ID
            - Have its taskType automatically set to STORY
            - Inherit the Epic's workspace and category if not specified
            - Be assigned sort orders automatically
            \s""")
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
}
