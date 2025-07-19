package com.omori.taskmanagement.springboot.controller;

import com.omori.taskmanagement.springboot.dto.project.CreateTaskRequest;
import com.omori.taskmanagement.springboot.dto.project.CreateTaskResponse;
import com.omori.taskmanagement.springboot.model.project.Task;
import com.omori.taskmanagement.springboot.repository.usermgmt.UserRepository;
import com.omori.taskmanagement.springboot.security.service.CustomUserDetails;
import com.omori.taskmanagement.springboot.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
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
}
