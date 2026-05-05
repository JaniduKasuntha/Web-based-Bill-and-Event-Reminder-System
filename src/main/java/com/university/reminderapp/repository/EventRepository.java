package com.university.reminderapp.repository;

import com.university.reminderapp.model.Event;
import com.university.reminderapp.model.University;
import com.university.reminderapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByUniversity(University university);
    List<Event> findByUniversityId(Long universityId);
    List<Event> findByUniversityAndStatus(University university, String status);

    @Query("SELECT e FROM Event e WHERE e.university.id = :universityId AND e.startsAt > :now ORDER BY e.startsAt ASC")
    List<Event> findUpcomingEvents(Long universityId, LocalDateTime now);

    List<Event> findByCreatedBy(User createdBy);
}