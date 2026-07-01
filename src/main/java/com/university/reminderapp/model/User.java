package com.university.reminderapp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(unique = true, nullable = false)
    private String email;

    private String firstName;
    private String lastName;
    private String status = "ACTIVE";

    private java.time.LocalDateTime createdAt;

    @ElementCollection(fetch = FetchType.EAGER)
    private Set<String> roles;

    @ManyToOne
    @JoinColumn(name = "university_id")
    private University university;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private User parent;

    @PrePersist
    protected void onCreate() {
        createdAt = java.time.LocalDateTime.now();
    }

    public String getFullName() {
        if (firstName == null && lastName == null) {
            return username != null ? username : email;
        }
        return (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
    }

    public String getRole() {
        if (roles != null && !roles.isEmpty()) {
            String r = roles.iterator().next();
            if (r.startsWith("ROLE_")) {
                return r.substring(5);
            }
            return r;
        }
        return null;
    }
}


