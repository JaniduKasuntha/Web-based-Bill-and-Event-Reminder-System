package com.university.reminderapp.controller.web;

import com.university.reminderapp.model.*;
import com.university.reminderapp.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class DashboardController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private UniversityRepository universityRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private BillRepository billRepository;

    @Autowired
    private BillNoticeRepository billNoticeRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return "redirect:/login";
        }

        String currentUsername = auth.getName();
        User user = userRepository.findByEmail(currentUsername)
                .or(() -> userRepository.findByUsername(currentUsername))
                .orElse(null);

        if (user == null) {
            return "redirect:/login?error";
        }

        String role = user.getRole();
        if (role == null) {
            return "redirect:/login?error";
        }

        if (role.equals("COMPANY_ADMIN")) {
            long totalUniversities = universityRepository.count();
            long activeUniversities = universityRepository.findAll().stream()
                    .filter(u -> "ACTIVE".equals(u.getStatus()))
                    .count();
            long totalAdmins = userRepository.findAll().stream()
                    .filter(u -> u.getRole() != null && u.getRole().equals("UNIVERSITY_ADMIN"))
                    .count();

            double activePercentage = totalUniversities > 0 ? ((double) activeUniversities / totalUniversities) * 100 : 0;

            model.addAttribute("totalUniversities", totalUniversities);
            model.addAttribute("activeUniversities", activeUniversities);
            model.addAttribute("activeUniversitiesPercentage", (int) activePercentage);
            model.addAttribute("totalAdmins", totalAdmins);
            model.addAttribute("recentUniversities", universityRepository.findAll());
            return "dashboards/company-admin";

        } else if (role.equals("UNIVERSITY_ADMIN")) {
            University university = user.getUniversity();
            if (university == null) {
                model.addAttribute("error", "No university assigned to this admin account.");
                return "error/403";
            }

            List<User> uniUsers = userRepository.findAllByUniversityId(university.getId());
            long studentCount = uniUsers.stream().filter(u -> "STUDENT".equals(u.getRole())).count();
            long eventManagerCount = uniUsers.stream().filter(u -> "EVENT_MANAGER".equals(u.getRole())).count();
            long parentCount = uniUsers.stream().filter(u -> "PARENT".equals(u.getRole())).count();

            List<Event> upcomingEvents = eventRepository.findUpcomingEvents(university.getId(), LocalDateTime.now());
            List<Bill> pendingBills = billRepository.findByUniversityId(university.getId()).stream()
                    .filter(b -> "PENDING".equals(b.getStatus()))
                    .collect(Collectors.toList());
            List<BillNotice> activeBillNotices = billNoticeRepository.findActiveNotices(university.getId(), LocalDate.now());

            model.addAttribute("university", university);
            model.addAttribute("studentCount", studentCount);
            model.addAttribute("eventManagerCount", eventManagerCount);
            model.addAttribute("parentCount", parentCount);
            model.addAttribute("eventCount", upcomingEvents.size());
            model.addAttribute("pendingBills", pendingBills);
            model.addAttribute("upcomingEvents", upcomingEvents);
            model.addAttribute("activeBillNotices", activeBillNotices);
            return "dashboards/university-admin";

        } else if (role.equals("STUDENT")) {
            University university = user.getUniversity();
            if (university == null) {
                model.addAttribute("error", "No university assigned to this student account.");
                return "error/403";
            }

            List<Bill> studentBills = billRepository.findByStudent(user);
            long pendingBillCount = studentBills.stream().filter(b -> "PENDING".equals(b.getStatus())).count();

            List<Event> upcomingEvents = eventRepository.findUpcomingEvents(university.getId(), LocalDateTime.now());
            List<Review> studentReviews = reviewRepository.findByUserId(user.getId());
            List<BillNotice> activeBillNotices = billNoticeRepository.findActiveNotices(university.getId(), LocalDate.now());

            model.addAttribute("student", user);
            model.addAttribute("billCount", studentBills.size());
            model.addAttribute("pendingBillCount", pendingBillCount);
            model.addAttribute("upcomingEventsCount", upcomingEvents.size());
            model.addAttribute("reviewCount", studentReviews.size());
            model.addAttribute("activeBillNotices", activeBillNotices);
            model.addAttribute("pendingBills", studentBills.stream().filter(b -> "PENDING".equals(b.getStatus())).collect(Collectors.toList()));
            model.addAttribute("upcomingEvents", upcomingEvents);
            return "dashboards/student";

        } else if (role.equals("PARENT")) {
            List<User> children = userRepository.findAllByParentId(user.getId());
            long pendingBillCount = 0;
            long upcomingEventsCount = 0;
            List<Bill> recentBills = new ArrayList<>();
            List<BillNotice> activeBillNotices = new ArrayList<>();
            List<Event> upcomingEvents = new ArrayList<>();
            Map<Long, Long> studentPendingBills = new HashMap<>();

            for (User student : children) {
                List<Bill> studentBills = billRepository.findByStudent(student);
                long studentPending = studentBills.stream().filter(b -> "PENDING".equals(b.getStatus())).count();
                studentPendingBills.put(student.getId(), studentPending);
                pendingBillCount += studentPending;
                recentBills.addAll(studentBills);

                if (student.getUniversity() != null) {
                    Long uniId = student.getUniversity().getId();
                    upcomingEvents.addAll(eventRepository.findUpcomingEvents(uniId, LocalDateTime.now()));
                    activeBillNotices.addAll(billNoticeRepository.findActiveNotices(uniId, LocalDate.now()));
                }
            }

            // Deduplicate lists
            upcomingEvents = upcomingEvents.stream().distinct().sorted(Comparator.comparing(Event::getStartsAt)).collect(Collectors.toList());
            activeBillNotices = activeBillNotices.stream().distinct().collect(Collectors.toList());

            model.addAttribute("parent", user);
            model.addAttribute("studentCount", children.size());
            model.addAttribute("pendingBillCount", pendingBillCount);
            model.addAttribute("upcomingEventsCount", upcomingEvents.size());
            model.addAttribute("students", children);
            model.addAttribute("studentPendingBills", studentPendingBills);
            model.addAttribute("recentBills", recentBills);
            model.addAttribute("activeBillNotices", activeBillNotices);
            model.addAttribute("upcomingEvents", upcomingEvents);
            return "dashboards/parent";

        } else if (role.equals("EVENT_MANAGER")) {
            List<Event> myEvents = eventRepository.findByCreatedBy(user);
            long upcomingCount = myEvents.stream().filter(e -> e.getStartsAt().isAfter(LocalDateTime.now()) && "SCHEDULED".equals(e.getStatus())).count();

            List<Review> myReviews = myEvents.stream()
                    .flatMap(e -> reviewRepository.findByEventId(e.getId()).stream())
                    .sorted(Comparator.comparing(Review::getCreatedAt).reversed())
                    .collect(Collectors.toList());

            model.addAttribute("totalEvents", myEvents.size());
            model.addAttribute("upcomingEventsCount", upcomingCount);
            model.addAttribute("totalReviews", myReviews.size());
            model.addAttribute("upcomingEvents", myEvents.stream().filter(e -> "SCHEDULED".equals(e.getStatus())).collect(Collectors.toList()));
            model.addAttribute("recentReviews", myReviews);
            return "dashboards/event-manager";
        }

        return "redirect:/login?error";
    }
}
