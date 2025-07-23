package com.omori.taskmanagement.springboot.repository.project;

import com.omori.taskmanagement.springboot.model.project.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

}
