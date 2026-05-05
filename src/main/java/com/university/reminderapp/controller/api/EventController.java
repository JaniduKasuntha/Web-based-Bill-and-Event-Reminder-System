package com.university.reminderapp.controller.api;

import com.university.reminderapp.dto.request.EventRequest;
import com.university.reminderapp.dto.response.ApiResponse;
import com.university.reminderapp.model.Event;
import com.university.reminderapp.model.User;
import com.university.reminderapp.service.EventService;
import com.university.reminderapp.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

        import java.util.List;

@RestController
@RequestMapping("/api/events")
public class EventController {
    @Autowired
    private EventService eventService;

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<List<Event>> getEvents(@RequestParam(required = false) Boolean upcoming) {
        User currentUser = userService.getCurrentUser();

        if (currentUser.getUniversity() != null) {
            if (Boolean.TRUE.equals(upcoming)) {
                return ResponseEntity.ok(eventService.getUpcomingEvents(currentUser.getUniversity().getId()));
            } else {
                return ResponseEntity.ok(eventService.getEventsByUniversity(currentUser.getUniversity().getId()));
            }
        }

        return ResponseEntity.ok(eventService.getAllEvents());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Event> getEventById(@PathVariable Long id) {
        return ResponseEntity.ok(eventService.getEventById(id));
    }

    @PostMapping
    public ResponseEntity<Event> createEvent(@Valid @RequestBody EventRequest request) {
        User currentUser = userService.getCurrentUser();

        if (currentUser.getRole().equals("EVENT_MANAGER") || currentUser.getRole().equals("UNIVERSITY_ADMIN")) {
            request.setUniversityId(currentUser.getUniversity().getId());
        }

        return new ResponseEntity<>(eventService.createEvent(request, currentUser), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Event> updateEvent(@PathVariable Long id, @Valid @RequestBody EventRequest request) {
        User currentUser = userService.getCurrentUser();
        return ResponseEntity.ok(eventService.updateEvent(id, request, currentUser));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<Event> cancelEvent(@PathVariable Long id) {
        User currentUser = userService.getCurrentUser();
        return ResponseEntity.ok(eventService.cancelEvent(id, currentUser));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteEvent(@PathVariable Long id) {
        eventService.deleteEvent(id, null);
        return ResponseEntity.ok(new ApiResponse(true, "Event deleted successfully"));

    }
}
