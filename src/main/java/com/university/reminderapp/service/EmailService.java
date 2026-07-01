package com.university.reminderapp.service;

import com.university.reminderapp.model.EmailHistory;
import com.university.reminderapp.model.User;
import com.university.reminderapp.repository.EmailHistoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private EmailHistoryRepository emailHistoryRepository;

    public void sendEmail(User recipient, String subject, String body) {
        logger.info("Sending Email to: {}, Subject: {}, Body: {}", recipient.getEmail(), subject, body);

        EmailHistory history = new EmailHistory();
        history.setRecipient(recipient);
        history.setSubject(subject);
        history.setBody(body);
        if (recipient.getUniversity() != null) {
            history.setUniversity(recipient.getUniversity());
        }
        emailHistoryRepository.save(history);
    }
}
