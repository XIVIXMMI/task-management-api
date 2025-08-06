package com.omori.taskmanagement.springboot.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
// import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;

import com.omori.taskmanagement.springboot.annotations.LogActivity;
import com.omori.taskmanagement.springboot.dto.common.ApiResponse;
import com.omori.taskmanagement.springboot.dto.project.SubtaskRequest;
import com.omori.taskmanagement.springboot.dto.project.SubtaskResponse;
import com.omori.taskmanagement.springboot.dto.project.SubtaskUpdateRequest;
import com.omori.taskmanagement.springboot.model.audit.ActionType;
import com.omori.taskmanagement.springboot.model.project.Subtask;
import com.omori.taskmanagement.springboot.service.SubTaskService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/subtasks")
@Tag(name = "Subtask", description = "Subtask management")
public class SubTaskController {
    
    private final SubTaskService subTaskService;

    @LogActivity(ActionType.CREATE)
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @PostMapping("/tasks/{task}")
    @Operation(summary = "Create subtask", description = "Create new subtask for a specific task id")
    public ResponseEntity<ApiResponse<SubtaskResponse>> createSubtask(
        @Valid @RequestBody SubtaskRequest request,
        @PathVariable @NotNull Long taskId
    ) {
        request.setTaskId(taskId);
        Subtask subtask = subTaskService.createSubtask(request);
        SubtaskResponse response  = SubtaskResponse.from(subtask);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @LogActivity(ActionType.VIEW)
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @GetMapping("/task/{taskId}")
    @Operation(summary = "List subtask", description = "Get all subtasks of task by task id")
    public ResponseEntity<ApiResponse<List<SubtaskResponse>>> getAllSubtaskByTaskId(
        @PathVariable @NotNull Long taskId
    ) {

        List<Subtask> subtask = subTaskService.getSubtasksByTaskId(taskId);
        List<SubtaskResponse> response = subtask.stream()
            .map(SubtaskResponse::from)
            .toList();

        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(response));
    }

    @LogActivity(ActionType.UPDATE)
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @PutMapping("/{subtaskId}")
    @Operation(summary = "Update Subtask", description = "Update subtask by specific taskId")
    public ResponseEntity<ApiResponse<SubtaskResponse>> updateSubtaskById(
        @PathVariable @NotNull Long subtaskId,
        @Valid @RequestBody SubtaskUpdateRequest request
    ) {
        Subtask subtask = subTaskService.updateSubtask(subtaskId, request);
        SubtaskResponse response = SubtaskResponse.from(subtask);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(response));
    }

    @LogActivity(ActionType.DELETE)
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @DeleteMapping("/{subtaskId}")
    @Operation(summary = "Delete Subtask", description = "Delete subtask by id")
    public ResponseEntity<ApiResponse<Void>> deleteSubtaskById(
        @PathVariable @NotNull Long subtaskId
    ) {
        subTaskService.deleteSubtask(subtaskId);
        return ResponseEntity.noContent().build();
    }

    @LogActivity(ActionType.DELETE)
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @DeleteMapping("/{subtaskId}/soft")
    @Operation(summary = "Soft delete Subtask", description = "Soft delete subtask by id")
    public ResponseEntity<ApiResponse<Void>> softDeleteSubtaskById(
        @PathVariable @NotNull Long subtaskId
    ) {
        subTaskService.softDeleteSubtask(subtaskId);
        return ResponseEntity.noContent().build();
    }

}
