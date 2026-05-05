package com.university.reminderapp.service;

import com.university.reminderapp.dto.request.EventRequest;
import com.university.reminderapp.exception.AccessDeniedException;
import com.university.reminderapp.exception.BadRequestException;
import com.university.reminderapp.exception.ResourceNotFoundException;
import com.university.reminderapp.model.Event;
import com.university.reminderapp.model.University;
import com.university.reminderapp.model.User;
import com.university.reminderapp.repository.EventRepository;
import com.university.reminderapp.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EventService {
    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UniversityService universityService;

    @Autowired
    private UserService userService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private ReviewRepository reviewRepository;

    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    public List<Event> getEventsByUniversity(Long universityId) {
        return eventRepository.findByUniversityId(universityId);
    }

    public List<Event> getUpcomingEvents(Long universityId) {
        return eventRepository.findUpcomingEvents(universityId, LocalDateTime.now());
    }

    public Event getEventById(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + id));
    }

    public Event createEvent(EventRequest request, User currentUser) {
        // Validate that end time is after start time
        if (request.getEndsAt().isBefore(request.getStartsAt())) {
            throw new BadRequestException("End time must be after start time");
        }

        University university = universityService.getUniversityById(request.getUniversityId());

        Event event = new Event();
        event.setUniversity(university);
        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setVenue(request.getVenue());
        event.setStartsAt(request.getStartsAt());
        event.setEndsAt(request.getEndsAt());
        event.setVisibility(request.getVisibility());
        event.setStatus(request.getStatus());
        event.setCreatedBy(currentUser);
        event.setUpdatedBy(currentUser);

        Event savedEvent = eventRepository.save(event);

        // Send notifications to all students of the university
        List<User> students = userService.getStudentsByUniversity(university.getId());
        for (User student : students) {
            notificationService.sendEventNotification(student, savedEvent, "New event announced");

            // Also notify parents
            if (student.getParent() != null) {
                notificationService.sendEventNotification(student.getParent(), savedEvent, "New event for your student");
            }
        }

        return savedEvent;
    }

    public Event updateEvent(Long id, EventRequest request, User currentUser) {
        // Validate that end time is after start time
        if (request.getEndsAt().isBefore(request.getStartsAt())) {
            throw new BadRequestException("End time must be after start time");
        }

        Event event = getEventById(id);

        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setVenue(request.getVenue());
        event.setStartsAt(request.getStartsAt());
        event.setEndsAt(request.getEndsAt());
        event.setVisibility(request.getVisibility());
        event.setStatus(request.getStatus());
        event.setUpdatedBy(currentUser);

        Event updatedEvent = eventRepository.save(event);

        // Send update notifications to all students of the university
        List<User> students = userService.getStudentsByUniversity(event.getUniversity().getId());
        for (User student : students) {
            notificationService.sendEventNotification(student, updatedEvent, "Event updated");
        }

        return updatedEvent;
    }

    public Event cancelEvent(Long id, User currentUser) {
        Event event = getEventById(id);

        event.setStatus("CANCELLED");
        event.setUpdatedBy(currentUser);

        Event cancelledEvent = eventRepository.save(event);

        // Send cancellation notifications to all students of the university
        List<User> students = userService.getStudentsByUniversity(event.getUniversity().getId());
        for (User student : students) {
            notificationService.sendEventNotification(student, cancelledEvent, "Event cancelled");

            // Also notify parents
            if (student.getParent() != null) {
                notificationService.sendEventNotification(student.getParent(), cancelledEvent, "Event cancelled");
            }
        }

        return cancelledEvent;
    }

    public void deleteEvent(Long id, User currentUser) {
        Event event = getEventById(id);

        if ("EVENT_MANAGER".equals(currentUser.getRole()) &&
                (currentUser.getUniversity() == null ||
                        !event.getUniversity().getId().equals(currentUser.getUniversity().getId()))) {
            throw new AccessDeniedException("You can only delete events from your university");
        }

        reviewRepository.deleteAll(reviewRepository.findByEventId(id));
        eventRepository.delete(event);
    }
}
