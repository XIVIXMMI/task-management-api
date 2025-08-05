package com.omori.taskmanagement.springboot.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;

import com.omori.taskmanagement.springboot.annotations.LogActivity;
import com.omori.taskmanagement.springboot.dto.common.ApiResponse;
import com.omori.taskmanagement.springboot.dto.project.SubtaskRequest;
import com.omori.taskmanagement.springboot.dto.project.SubtaskResponse;
import com.omori.taskmanagement.springboot.model.audit.ActionType;
import com.omori.taskmanagement.springboot.model.project.Subtask;
import com.omori.taskmanagement.springboot.service.SubTaskService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@Tag(name = "Subtask", description = "Subtask management")
public class SubTaskController {
    
    private final SubTaskService subTaskService;

    @LogActivity(ActionType.CREATE)
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @PostMapping("/tasks/{taskId}/subtasks")
    @Operation(summary = "Create subtask", description = "Create new subtask for a specific task id")
    public ResponseEntity<ApiResponse<SubtaskResponse>> createSubtask(
        @Valid @RequestBody SubtaskRequest request,
        @PathVariable Long taskId
    ) {

        Subtask subtask = subTaskService.createSubtask(request);
        SubtaskResponse response  = SubtaskResponse.from(subtask);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }
}
