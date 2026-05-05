package com.university.reminderapp.repository;

import com.university.reminderapp.model.Notification;
import com.university.reminderapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUser(User user);
    List<Notification> findByUserAndIsRead(User user, boolean isRead);
    long countByUserAndIsRead(User user, boolean isRead);
}


