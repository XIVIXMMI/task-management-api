package com.omori.taskmanagement.dto.project.subtask;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SubtaskReorderDTO {
    
    private List<Long> subtaskIds;

}
