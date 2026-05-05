package com.university.reminderapp.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "delivery_log")
public class DeliveryLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "notification_id", nullable = false)
    private Notification notification;

    private String status;

    private LocalDateTime deliveredAt;

    @PrePersist
    protected void onCreate() {
        deliveredAt = LocalDateTime.now();
    }
}

