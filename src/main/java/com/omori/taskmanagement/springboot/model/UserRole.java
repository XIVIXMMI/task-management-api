package com.omori.taskmanagement.springboot.model;

import jakarta.persistence.*;
import lombok.*;

@Data
@Entity
@Table(name = "user_roles")
public class UserRole {

    public static final String USER = null;

    @Id
    private Short id;

    @Column(nullable = false, unique = true)
    private String name;

    private String description;
}
