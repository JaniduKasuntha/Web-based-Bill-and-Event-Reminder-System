package com.university.reminderapp.controller.api;

import com.university.reminderapp.dto.response.ApiResponse;
import com.university.reminderapp.model.DeliveryLog;
import com.university.reminderapp.model.Notification;
import com.university.reminderapp.model.User;
import com.university.reminderapp.service.NotificationService;
import com.university.reminderapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

        import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<List<Notification>> getNotifications(@RequestParam(required = false) Boolean unread) {
        User currentUser = userService.getCurrentUser();

        if (Boolean.TRUE.equals(unread)) {
            return ResponseEntity.ok(notificationService.getUnreadNotificationsForUser(currentUser));
        } else {
            return ResponseEntity.ok(notificationService.getNotificationsForUser(currentUser));
        }
    }

    @GetMapping("/count")
    public ResponseEntity<Integer> getUnreadCount() {
        User currentUser = userService.getCurrentUser();
        return ResponseEntity.ok(notificationService.getUnreadNotificationCount(currentUser));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Notification> getNotificationById(@PathVariable Long id) {
        return ResponseEntity.ok(notificationService.getNotificationById(id));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<Notification> markAsRead(@PathVariable Long id) {
        User currentUser = userService.getCurrentUser();
        return ResponseEntity.ok(notificationService.markAsRead(id, currentUser));
    }

    @GetMapping("/{id}/logs")
    public ResponseEntity<List<DeliveryLog>> getDeliveryLogs(@PathVariable Long id) {
        return ResponseEntity.ok(notificationService.getDeliveryLogsForNotification(id));
    }
}