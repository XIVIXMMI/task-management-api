package com.omori.taskmanagement.springboot.repository.project;

import com.omori.taskmanagement.springboot.model.project.Task;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task,Long> {


}
