package com.university.reminderapp.controller.web;

import com.university.reminderapp.model.Bill;
import com.university.reminderapp.model.Event;
import com.university.reminderapp.model.User;
import com.university.reminderapp.repository.BillRepository;
import com.university.reminderapp.repository.EventRepository;
import com.university.reminderapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/parent")
public class ParentWebController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BillRepository billRepository;

    @Autowired
    private EventRepository eventRepository;

    private User getCurrentParent() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return null;
        String name = auth.getName();
        return userRepository.findByEmail(name)
                .or(() -> userRepository.findByUsername(name))
                .orElse(null);
    }

    @GetMapping("/students")
    public String listStudents(Model model) {
        User parent = getCurrentParent();
        if (parent == null) return "redirect:/login";

        List<User> students = userRepository.findAllByParentId(parent.getId());
        Map<Long, Long> pendingCounts = new HashMap<>();

        for (User student : students) {
            long count = billRepository.findByStudent(student).stream()
                    .filter(b -> "PENDING".equals(b.getStatus()))
                    .count();
            pendingCounts.put(student.getId(), count);
        }

        model.addAttribute("students", students);
        model.addAttribute("pendingCounts", pendingCounts);
        return "parent/parent-student-list";
    }

    @GetMapping("/students/{id}")
    public String viewStudentDetail(@PathVariable("id") Long id, Model model) {
        User parent = getCurrentParent();
        User student = userRepository.findById(id).orElse(null);
        if (parent == null || student == null) return "redirect:/login";

        // Check relationship safety
        if (student.getParent() == null || !student.getParent().getId().equals(parent.getId())) {
            model.addAttribute("error", "Access Denied: Student is not linked to your account.");
            return "error/403";
        }

        List<Bill> bills = billRepository.findByStudent(student);
        List<Event> upcomingEvents = List.of();
        if (student.getUniversity() != null) {
            upcomingEvents = eventRepository.findUpcomingEvents(student.getUniversity().getId(), LocalDateTime.now());
        }

        model.addAttribute("student", student);
        model.addAttribute("bills", bills);
        model.addAttribute("upcomingEvents", upcomingEvents);
        return "parent/parent-student-detail";
    }
}
