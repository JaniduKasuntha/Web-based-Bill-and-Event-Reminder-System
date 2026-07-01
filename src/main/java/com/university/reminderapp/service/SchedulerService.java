package com.university.reminderapp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class SchedulerService {
    private static final Logger logger = LoggerFactory.getLogger(SchedulerService.class);

    // Runs every hour to check for upcoming events or bills (stub schedule)
    @Scheduled(cron = "0 0 * * * *")
    public void checkReminders() {
        logger.info("Scheduler checking upcoming bills and events...");
    }
}
