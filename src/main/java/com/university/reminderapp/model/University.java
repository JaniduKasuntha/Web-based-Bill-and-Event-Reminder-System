package com.university.reminderapp.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "university")
public class University {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;
}

