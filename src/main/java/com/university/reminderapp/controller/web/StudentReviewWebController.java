package com.university.reminderapp.controller.web;

import com.university.reminderapp.dto.request.ReviewRequest;
import com.university.reminderapp.model.Event;
import com.university.reminderapp.model.Review;
import com.university.reminderapp.model.User;
import com.university.reminderapp.repository.EventRepository;
import com.university.reminderapp.repository.ReviewRepository;
import com.university.reminderapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/student/reviews")
public class StudentReviewWebController {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    private User getCurrentStudent() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return null;
        String name = auth.getName();
        return userRepository.findByEmail(name)
                .or(() -> userRepository.findByUsername(name))
                .orElse(null);
    }

    @GetMapping
    public String listMyReviews(Model model) {
        User student = getCurrentStudent();
        if (student == null) return "redirect:/login";

        List<Review> reviews = reviewRepository.findByUserId(student.getId());
        model.addAttribute("reviews", reviews);
        return "review/student-review-list";
    }

    @GetMapping("/create")
    public String showCreateForm(@RequestParam("eventId") Long eventId, Model model) {
        User student = getCurrentStudent();
        Event event = eventRepository.findById(eventId).orElse(null);
        if (student == null || event == null) {
            model.addAttribute("error", "Event not found.");
            return "error/404";
        }

        ReviewRequest request = new ReviewRequest();
        request.setEventId(eventId);

        model.addAttribute("event", event);
        model.addAttribute("review", request);
        model.addAttribute("reviewId", null);
        return "review/review-form";
    }

    @PostMapping("/create")
    public String createReview(@ModelAttribute("review") ReviewRequest request, BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        User student = getCurrentStudent();
        Event event = eventRepository.findById(request.getEventId()).orElse(null);
        if (student == null || event == null) {
            return "redirect:/dashboard";
        }

        if (result.hasErrors()) {
            model.addAttribute("event", event);
            model.addAttribute("reviewId", null);
            return "review/review-form";
        }

        Review review = new Review();
        review.setEvent(event);
        review.setUser(student);
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        reviewRepository.save(review);

        redirectAttributes.addFlashAttribute("success", "Review submitted successfully.");
        return "redirect:/student/reviews";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        User student = getCurrentStudent();
        Review review = reviewRepository.findById(id).orElse(null);
        if (student == null || review == null) {
            model.addAttribute("error", "Review not found.");
            return "error/404";
        }

        // Security check: Only the author can edit
        if (!review.getUser().getId().equals(student.getId())) {
            model.addAttribute("error", "Access Denied: You did not write this review.");
            return "error/403";
        }

        ReviewRequest request = new ReviewRequest();
        request.setEventId(review.getEvent().getId());
        request.setRating(review.getRating());
        request.setComment(review.getComment());

        model.addAttribute("event", review.getEvent());
        model.addAttribute("review", request);
        model.addAttribute("reviewId", id);
        return "review/review-form";
    }

    @PostMapping("/{id}/edit")
    public String editReview(@PathVariable("id") Long id, @ModelAttribute("review") ReviewRequest request, BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        User student = getCurrentStudent();
        Review review = reviewRepository.findById(id).orElse(null);
        if (student == null || review == null) {
            return "redirect:/student/reviews";
        }

        if (!review.getUser().getId().equals(student.getId())) {
            return "redirect:/student/reviews";
        }

        if (result.hasErrors()) {
            model.addAttribute("event", review.getEvent());
            model.addAttribute("reviewId", id);
            return "review/review-form";
        }

        review.setRating(request.getRating());
        review.setComment(request.getComment());
        reviewRepository.save(review);

        redirectAttributes.addFlashAttribute("success", "Review updated successfully.");
        return "redirect:/student/reviews";
    }

    @PostMapping("/{id}/delete")
    public String deleteReview(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        User student = getCurrentStudent();
        Review review = reviewRepository.findById(id).orElse(null);
        if (student != null && review != null && review.getUser().getId().equals(student.getId())) {
            reviewRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Review deleted successfully.");
        }
        return "redirect:/student/reviews";
    }
}
