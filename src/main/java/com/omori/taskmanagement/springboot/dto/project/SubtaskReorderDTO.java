package com.omori.taskmanagement.springboot.dto.project;

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
