
package com.university.reminderapp.controller.web;

import com.university.reminderapp.dto.request.EventRequest;
import com.university.reminderapp.model.Event;
import com.university.reminderapp.model.User;
import com.university.reminderapp.service.EventService;
import com.university.reminderapp.service.ReviewService;
import com.university.reminderapp.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
        import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/events")
public class EventWebController {

    @Autowired
    private EventService eventService;

    @Autowired
    private UserService userService;

    @Autowired
    private ReviewService reviewService;

    @GetMapping
    public String listEvents(@RequestParam(required = false) String status,
                             @RequestParam(required = false) String upcoming,
                             @RequestParam(required = false) String venue,
                             Model model) {
        User currentUser = userService.getCurrentUser();

        List<Event> events;
        if (currentUser != null && currentUser.getUniversity() != null) {
            events = eventService.getEventsByUniversity(currentUser.getUniversity().getId());
        } else {
            events = eventService.getAllEvents();
        }

        LocalDateTime now = LocalDateTime.now();

        if (status != null && !status.isBlank()) {
            events = events.stream()
                    .filter(event -> status.equalsIgnoreCase(event.getStatus()))
                    .collect(Collectors.toList());
        }

        if (upcoming != null && !upcoming.isBlank()) {
            if ("true".equalsIgnoreCase(upcoming)) {
                events = events.stream()
                        .filter(event -> event.getStartsAt() != null && !event.getStartsAt().isBefore(now))
                        .collect(Collectors.toList());
            } else if ("past".equalsIgnoreCase(upcoming)) {
                events = events.stream()
                        .filter(event -> event.getEndsAt() != null && event.getEndsAt().isBefore(now))
                        .collect(Collectors.toList());
            }
        }

        if (venue != null && !venue.isBlank()) {
            String venueFilter = venue.toLowerCase();
            events = events.stream()
                    .filter(event -> event.getVenue() != null && event.getVenue().toLowerCase().contains(venueFilter))
                    .collect(Collectors.toList());
        }

        model.addAttribute("events", events);
        return "event/event-list";
    }

    @PreAuthorize("hasAnyRole('UNIVERSITY_ADMIN','EVENT_MANAGER')")
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        User currentUser = userService.getCurrentUser();
        EventRequest eventRequest = new EventRequest();

        if (currentUser != null && currentUser.getUniversity() != null) {
            eventRequest.setUniversityId(currentUser.getUniversity().getId());
        }

        model.addAttribute("event", eventRequest);
        model.addAttribute("eventId", null);
        return "event/event-form";
    }

    @PreAuthorize("hasAnyRole('UNIVERSITY_ADMIN','EVENT_MANAGER')")
    @PostMapping("/create")
    public String createEvent(@Valid @ModelAttribute("event") EventRequest request,
                              BindingResult result,
                              RedirectAttributes redirectAttributes,
                              Model model) {
        User currentUser = userService.getCurrentUser();

        if (currentUser != null && currentUser.getUniversity() != null) {
            request.setUniversityId(currentUser.getUniversity().getId());
        }

        if (result.hasErrors()) {
            model.addAttribute("eventId", null);
            return "event/event-form";
        }

        eventService.createEvent(request, currentUser);
        redirectAttributes.addFlashAttribute("success", "Event created successfully!");
        return "redirect:/events";
    }

    @GetMapping("/{id}")
    public String viewEvent(@PathVariable Long id, Model model) {
        Event event = eventService.getEventById(id);
        model.addAttribute("event", event);
        model.addAttribute("reviews", reviewService.getReviewsByEvent(id));
        return "event/event-detail";
    }

    @PreAuthorize("hasAnyRole('UNIVERSITY_ADMIN','EVENT_MANAGER')")
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        Event event = eventService.getEventById(id);

        EventRequest eventRequest = new EventRequest();
        eventRequest.setUniversityId(event.getUniversity().getId());
        eventRequest.setTitle(event.getTitle());
        eventRequest.setDescription(event.getDescription());
        eventRequest.setVenue(event.getVenue());
        eventRequest.setStartsAt(event.getStartsAt());
        eventRequest.setEndsAt(event.getEndsAt());
        eventRequest.setVisibility(event.getVisibility());
        eventRequest.setStatus(event.getStatus());

        model.addAttribute("event", eventRequest);
        model.addAttribute("eventId", id);
        return "event/event-form";
    }

    @PreAuthorize("hasAnyRole('UNIVERSITY_ADMIN','EVENT_MANAGER')")
    @PostMapping("/{id}/edit")
    public String updateEvent(@PathVariable Long id,
                              @Valid @ModelAttribute("event") EventRequest request,
                              BindingResult result,
                              RedirectAttributes redirectAttributes,
                              Model model) {
        User currentUser = userService.getCurrentUser();

        if (currentUser != null && currentUser.getUniversity() != null) {
            request.setUniversityId(currentUser.getUniversity().getId());
        }

        if (result.hasErrors()) {
            model.addAttribute("eventId", id);
            return "event/event-form";
        }

        eventService.updateEvent(id, request, currentUser);
        redirectAttributes.addFlashAttribute("success", "Event updated successfully!");
        return "redirect:/events/" + id;
    }

    @PreAuthorize("hasAnyRole('UNIVERSITY_ADMIN','EVENT_MANAGER')")
    @PostMapping("/{id}/cancel")
    public String cancelEvent(@PathVariable Long id,
                              RedirectAttributes redirectAttributes) {
        User currentUser = userService.getCurrentUser();
        eventService.cancelEvent(id, currentUser);
        redirectAttributes.addFlashAttribute("success", "Event cancelled successfully!");
        return "redirect:/events/" + id;
    }

    @PreAuthorize("hasAnyRole('UNIVERSITY_ADMIN','EVENT_MANAGER')")
    @PostMapping("/{id}/delete")
    public String deleteEvent(@PathVariable Long id,
                              @RequestParam(name = "returnTo", required = false) String returnTo,
                              RedirectAttributes redirectAttributes) {
        User currentUser = userService.getCurrentUser();
        eventService.deleteEvent(id, currentUser);
        redirectAttributes.addFlashAttribute("success", "Event deleted successfully!");

        if ("event-manager-dashboard".equals(returnTo)) {
            return "redirect:/event-manager-dashboard";
        }

        return "redirect:/events";
    }
}
