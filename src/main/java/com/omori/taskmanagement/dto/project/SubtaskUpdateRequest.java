package com.omori.taskmanagement.dto.project;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SubtaskUpdateRequest {

    private String title;
    private String description;
    private Integer sortOrder;
}
