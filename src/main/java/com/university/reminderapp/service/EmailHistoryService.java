package com.university.reminderapp.service;

import com.university.reminderapp.model.EmailHistory;
import com.university.reminderapp.repository.EmailHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class EmailHistoryService {

    @Autowired
    private EmailHistoryRepository emailHistoryRepository;

    public List<EmailHistory> getEmailsByUniversity(Long universityId) {
        return emailHistoryRepository.findAllByUniversityIdOrderBySentAtDesc(universityId);
    }

    public EmailHistory getEmailById(Long id) {
        return emailHistoryRepository.findById(id).orElse(null);
    }

    public EmailHistory saveEmailHistory(EmailHistory history) {
        return emailHistoryRepository.save(history);
    }

    public void deleteEmailHistory(Long id) {
        emailHistoryRepository.deleteById(id);
    }
}
