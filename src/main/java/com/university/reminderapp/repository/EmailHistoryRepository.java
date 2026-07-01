package com.university.reminderapp.repository;

import com.university.reminderapp.model.EmailHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EmailHistoryRepository extends JpaRepository<EmailHistory, Long> {
    List<EmailHistory> findAllByUniversityIdOrderBySentAtDesc(Long universityId);
}
