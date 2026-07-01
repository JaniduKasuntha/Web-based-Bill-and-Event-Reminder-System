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

    private String description;
    private String address;
    private String contactEmail;
    private String contactPhone;

    @Column(nullable = false)
    private String status = "ACTIVE";

    @Column(nullable = false)
    private java.time.LocalDateTime createdAt;

    private java.time.LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "company_id")
    private Company company;

    @PrePersist
    protected void onCreate() {
        createdAt = java.time.LocalDateTime.now();
        updatedAt = java.time.LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = java.time.LocalDateTime.now();
    }
}

