package com.university.reminderapp.service;

import com.university.reminderapp.model.Bill;
import com.university.reminderapp.model.BillNotice;
import com.university.reminderapp.model.DeliveryLog;
import com.university.reminderapp.model.Event;

import com.university.reminderapp.model.Notification;
import com.university.reminderapp.model.User;

import com.university.reminderapp.repository.DeliveryLogRepository;
import com.university.reminderapp.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private DeliveryLogRepository deliveryLogRepository;

    public void sendBillNotification(User user, Bill bill, String message) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setMessage(message + " for Bill #" + bill.getId());
        notification.setType("BILL");
        notificationRepository.save(notification);

        // Simulate delivery log
        DeliveryLog log = new DeliveryLog();
        log.setNotification(notification);
        log.setStatus("SENT");
        deliveryLogRepository.save(log);
    }

    public void sendEventNotification(User user, Event event, String message) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setMessage(message + " for Event: " + event.getTitle());
        notification.setType("EVENT");
        notificationRepository.save(notification);

        DeliveryLog log = new DeliveryLog();
        log.setNotification(notification);
        log.setStatus("SENT");
        deliveryLogRepository.save(log);
    }

    public void sendBillNoticeNotification(User user, BillNotice notice) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setMessage("New Bill Notice: " + notice.getTitle());
        notification.setType("BILL_NOTICE");
        notificationRepository.save(notification);

        DeliveryLog log = new DeliveryLog();
        log.setNotification(notification);
        log.setStatus("SENT");
        deliveryLogRepository.save(log);
    }

    public List<DeliveryLog> getDeliveryLogsForNotification(Long notificationId) {


        return deliveryLogRepository.findByNotificationId(notificationId);
    }

    public List<Notification> getUnreadNotificationsForUser(User user) {
        return notificationRepository.findByUserAndIsRead(user, false);
    }

    public List<Notification> getNotificationsForUser(User user) {
        return notificationRepository.findByUser(user);
    }

    public int getUnreadNotificationCount(User user) {
        return (int) notificationRepository.countByUserAndIsRead(user, false);
    }

    public Notification getNotificationById(Long id) {
        return notificationRepository.findById(id).orElse(null);
    }

    public Notification markAsRead(Long id, User user) {
        Notification notification = getNotificationById(id);
        if (notification != null && notification.getUser().getId().equals(user.getId())) {
            notification.setRead(true);
            return notificationRepository.save(notification);
        }
        return notification;
    }
}


