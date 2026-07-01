package com.university.reminderapp.controller.api;

import com.university.reminderapp.model.Event;
import com.university.reminderapp.model.Review;
import com.university.reminderapp.model.User;
import com.university.reminderapp.repository.EventRepository;
import com.university.reminderapp.repository.ReviewRepository;
import com.university.reminderapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return null;
        String name = auth.getName();
        return userRepository.findByEmail(name)
                .or(() -> userRepository.findByUsername(name))
                .orElse(null);
    }

    @GetMapping
    public List<Review> getAllReviews(@RequestParam(value = "eventId", required = false) Long eventId) {
        if (eventId != null) {
            return reviewRepository.findByEventId(eventId);
        }
        return reviewRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Review> getReviewById(@PathVariable("id") Long id) {
        Review review = reviewRepository.findById(id).orElse(null);
        if (review == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(review);
    }

    @PostMapping
    public ResponseEntity<Review> createReview(@RequestParam("eventId") Long eventId, @RequestBody Review review) {
        User user = getCurrentUser();
        Event event = eventRepository.findById(eventId).orElse(null);
        if (user == null || event == null) {
            return ResponseEntity.badRequest().build();
        }
        review.setUser(user);
        review.setEvent(event);
        Review saved = reviewRepository.save(review);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable("id") Long id) {
        Review review = reviewRepository.findById(id).orElse(null);
        if (review == null) {
            return ResponseEntity.notFound().build();
        }
        reviewRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
