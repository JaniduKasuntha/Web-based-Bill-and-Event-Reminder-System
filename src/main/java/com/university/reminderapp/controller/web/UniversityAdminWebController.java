package com.university.reminderapp.controller.web;

import com.university.reminderapp.dto.request.EmailHistoryRequest;
import com.university.reminderapp.dto.request.UniversityAdminProfileRequest;
import com.university.reminderapp.dto.request.UserRequest;
import com.university.reminderapp.model.EmailHistory;
import com.university.reminderapp.model.Notification;
import com.university.reminderapp.model.University;
import com.university.reminderapp.model.User;
import com.university.reminderapp.repository.NotificationRepository;
import com.university.reminderapp.repository.UserRepository;
import com.university.reminderapp.service.EmailHistoryService;
import com.university.reminderapp.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/university")
public class UniversityAdminWebController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private EmailHistoryService emailHistoryService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User getCurrentAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return null;
        String name = auth.getName();
        return userRepository.findByEmail(name)
                .or(() -> userRepository.findByUsername(name))
                .orElse(null);
    }

    // Manage Users
    @GetMapping("/users")
    public String listUsers(@RequestParam(value = "role", required = false) String role,
                            @RequestParam(value = "status", required = false) String status,
                            Model model) {
        User admin = getCurrentAdmin();
        if (admin == null || admin.getUniversity() == null) return "redirect:/login";

        University university = admin.getUniversity();
        List<User> users = userRepository.findAllByUniversityId(university.getId());

        // Filter in memory for robust role handling
        if (role != null && !role.trim().isEmpty()) {
            users = users.stream()
                    .filter(u -> u.getRoles() != null && u.getRoles().contains("ROLE_" + role))
                    .collect(Collectors.toList());
        }
        if (status != null && !status.trim().isEmpty()) {
            users = users.stream()
                    .filter(u -> status.equalsIgnoreCase(u.getStatus()))
                    .collect(Collectors.toList());
        }

        model.addAttribute("university", university);
        model.addAttribute("users", users);
        return "admin/university/university-user-list";
    }

    @GetMapping("/users/create")
    public String showCreateUserForm(Model model) {
        User admin = getCurrentAdmin();
        if (admin == null || admin.getUniversity() == null) return "redirect:/login";

        University university = admin.getUniversity();
        UserRequest userRequest = new UserRequest();
        userRequest.setUniversityId(university.getId());
        userRequest.setRole("STUDENT");

        // Fetch parent users for parent selection dropdown
        List<User> parents = userRepository.findAllByUniversityId(university.getId()).stream()
                .filter(u -> u.getRoles() != null && u.getRoles().contains("ROLE_PARENT"))
                .collect(Collectors.toList());

        model.addAttribute("university", university);
        model.addAttribute("user", userRequest);
        model.addAttribute("parents", parents);
        model.addAttribute("userId", null);
        return "admin/university/university-user-form";
    }

    @PostMapping("/users/create")
    public String createUser(@ModelAttribute("user") UserRequest request, BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        User admin = getCurrentAdmin();
        if (admin == null || admin.getUniversity() == null) return "redirect:/login";
        University university = admin.getUniversity();

        if (userRepository.existsByEmail(request.getEmail())) {
            result.rejectValue("email", "error.user", "Email already exists.");
        }

        if (result.hasErrors()) {
            List<User> parents = userRepository.findAllByUniversityId(university.getId()).stream()
                    .filter(u -> u.getRoles() != null && u.getRoles().contains("ROLE_PARENT"))
                    .collect(Collectors.toList());
            model.addAttribute("university", university);
            model.addAttribute("parents", parents);
            model.addAttribute("userId", null);
            return "admin/university/university-user-form";
        }

        User user = new User();
        user.setUsername(request.getEmail());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setStatus("ACTIVE");
        user.setUniversity(university);
        user.setRoles(new HashSet<>(Collections.singletonList("ROLE_" + request.getRole())));

        if ("STUDENT".equals(request.getRole()) && request.getParentId() != null) {
            userRepository.findById(request.getParentId()).ifPresent(user::setParent);
        }

        userRepository.save(user);
        redirectAttributes.addFlashAttribute("success", "User created successfully.");
        return "redirect:/admin/university/users";
    }

    @GetMapping("/users/{id}/edit")
    public String showEditUserForm(@PathVariable("id") Long id, Model model) {
        User admin = getCurrentAdmin();
        User user = userRepository.findById(id).orElse(null);
        if (admin == null || admin.getUniversity() == null || user == null) return "redirect:/login";

        University university = admin.getUniversity();
        UserRequest userRequest = new UserRequest();
        userRequest.setUniversityId(university.getId());
        userRequest.setFirstName(user.getFirstName());
        userRequest.setLastName(user.getLastName());
        userRequest.setEmail(user.getEmail());
        userRequest.setStatus(user.getStatus());

        String rawRole = user.getRole();
        userRequest.setRole(rawRole);
        if (user.getParent() != null) {
            userRequest.setParentId(user.getParent().getId());
        }

        List<User> parents = userRepository.findAllByUniversityId(university.getId()).stream()
                .filter(u -> u.getRoles() != null && u.getRoles().contains("ROLE_PARENT"))
                .collect(Collectors.toList());

        model.addAttribute("university", university);
        model.addAttribute("user", userRequest);
        model.addAttribute("parents", parents);
        model.addAttribute("userId", id);
        return "admin/university/university-user-form";
    }

    @PostMapping("/users/{id}/edit")
    public String editUser(@PathVariable("id") Long id, @ModelAttribute("user") UserRequest request, BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        User admin = getCurrentAdmin();
        User user = userRepository.findById(id).orElse(null);
        if (admin == null || admin.getUniversity() == null || user == null) return "redirect:/login";
        University university = admin.getUniversity();

        if (!user.getEmail().equals(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
            result.rejectValue("email", "error.user", "Email already exists.");
        }

        if (result.hasErrors()) {
            List<User> parents = userRepository.findAllByUniversityId(university.getId()).stream()
                    .filter(u -> u.getRoles() != null && u.getRoles().contains("ROLE_PARENT"))
                    .collect(Collectors.toList());
            model.addAttribute("university", university);
            model.addAttribute("parents", parents);
            model.addAttribute("userId", id);
            return "admin/university/university-user-form";
        }

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setUsername(request.getEmail());
        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }
        user.setRoles(new HashSet<>(Collections.singletonList("ROLE_" + request.getRole())));

        if ("STUDENT".equals(request.getRole()) && request.getParentId() != null) {
            userRepository.findById(request.getParentId()).ifPresent(user::setParent);
        } else {
            user.setParent(null);
        }

        userRepository.save(user);
        redirectAttributes.addFlashAttribute("success", "User updated successfully.");
        return "redirect:/admin/university/users";
    }

    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        userRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("success", "User deleted successfully.");
        return "redirect:/admin/university/users";
    }

    // Email management
    @GetMapping("/emails")
    public String listEmails(Model model) {
        User admin = getCurrentAdmin();
        if (admin == null || admin.getUniversity() == null) return "redirect:/login";

        model.addAttribute("university", admin.getUniversity());
        model.addAttribute("emails", emailHistoryService.getEmailsByUniversity(admin.getUniversity().getId()));
        return "admin/university/email-list";
    }

    @GetMapping("/emails/create")
    public String showComposeEmailForm(Model model) {
        User admin = getCurrentAdmin();
        if (admin == null || admin.getUniversity() == null) return "redirect:/login";

        List<User> recipients = userRepository.findAllByUniversityId(admin.getUniversity().getId()).stream()
                .filter(u -> !"UNIVERSITY_ADMIN".equals(u.getRole()))
                .collect(Collectors.toList());

        model.addAttribute("university", admin.getUniversity());
        model.addAttribute("email", new EmailHistoryRequest());
        model.addAttribute("recipients", recipients);
        model.addAttribute("isEdit", false);
        return "admin/university/email-form";
    }

    @PostMapping("/emails/create")
    public String sendEmail(@ModelAttribute("email") EmailHistoryRequest request, BindingResult result, RedirectAttributes redirectAttributes, Model model) {
        User admin = getCurrentAdmin();
        if (admin == null || admin.getUniversity() == null) return "redirect:/login";

        if (result.hasErrors()) {
            List<User> recipients = userRepository.findAllByUniversityId(admin.getUniversity().getId()).stream()
                    .filter(u -> !"UNIVERSITY_ADMIN".equals(u.getRole()))
                    .collect(Collectors.toList());
            model.addAttribute("university", admin.getUniversity());
            model.addAttribute("recipients", recipients);
            model.addAttribute("isEdit", false);
            return "admin/university/email-form";
        }

        User recipient = userRepository.findById(request.getRecipientId()).orElse(null);
        if (recipient != null) {
            emailService.sendEmail(recipient, request.getSubject(), request.getBody());
            redirectAttributes.addFlashAttribute("success", "Email sent successfully.");
        } else {
            redirectAttributes.addFlashAttribute("error", "Recipient not found.");
        }
        return "redirect:/admin/university/emails";
    }

    @GetMapping("/emails/{id}/edit")
    public String showEditEmailForm(@PathVariable("id") Long id, Model model) {
        User admin = getCurrentAdmin();
        EmailHistory history = emailHistoryService.getEmailById(id);
        if (admin == null || admin.getUniversity() == null || history == null) return "redirect:/login";

        EmailHistoryRequest request = new EmailHistoryRequest();
        request.setRecipientId(history.getRecipient().getId());
        request.setSubject(history.getSubject());
        request.setBody(history.getBody());

        List<User> recipients = userRepository.findAllByUniversityId(admin.getUniversity().getId()).stream()
                .filter(u -> !"UNIVERSITY_ADMIN".equals(u.getRole()))
                .collect(Collectors.toList());

        model.addAttribute("university", admin.getUniversity());
        model.addAttribute("email", request);
        model.addAttribute("recipients", recipients);
        model.addAttribute("emailId", id);
        model.addAttribute("isEdit", true);
        return "admin/university/email-form";
    }

    @PostMapping("/emails/{id}/edit")
    public String editAndResendEmail(@PathVariable("id") Long id, @ModelAttribute("email") EmailHistoryRequest request, BindingResult result, RedirectAttributes redirectAttributes, Model model) {
        User admin = getCurrentAdmin();
        if (admin == null || admin.getUniversity() == null) return "redirect:/login";

        if (result.hasErrors()) {
            List<User> recipients = userRepository.findAllByUniversityId(admin.getUniversity().getId()).stream()
                    .filter(u -> !"UNIVERSITY_ADMIN".equals(u.getRole()))
                    .collect(Collectors.toList());
            model.addAttribute("university", admin.getUniversity());
            model.addAttribute("recipients", recipients);
            model.addAttribute("emailId", id);
            model.addAttribute("isEdit", true);
            return "admin/university/email-form";
        }

        User recipient = userRepository.findById(request.getRecipientId()).orElse(null);
        if (recipient != null) {
            emailService.sendEmail(recipient, request.getSubject(), request.getBody());
            emailHistoryService.deleteEmailHistory(id); // remove older log
            redirectAttributes.addFlashAttribute("success", "Email updated and sent successfully.");
        } else {
            redirectAttributes.addFlashAttribute("error", "Recipient not found.");
        }
        return "redirect:/admin/university/emails";
    }

    @PostMapping("/emails/{id}/delete")
    public String deleteEmailRecord(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        emailHistoryService.deleteEmailHistory(id);
        redirectAttributes.addFlashAttribute("success", "Email record deleted.");
        return "redirect:/admin/university/emails";
    }

    // Profile Management
    @GetMapping("/profile")
    public String showProfileForm(Model model) {
        User admin = getCurrentAdmin();
        if (admin == null || admin.getUniversity() == null) return "redirect:/login";

        UniversityAdminProfileRequest request = new UniversityAdminProfileRequest();
        request.setFirstName(admin.getFirstName());
        request.setLastName(admin.getLastName());
        request.setEmail(admin.getEmail());

        model.addAttribute("university", admin.getUniversity());
        model.addAttribute("admin", request);
        return "admin/university/admin-profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@ModelAttribute("admin") UniversityAdminProfileRequest request, BindingResult result, RedirectAttributes redirectAttributes, Model model) {
        User admin = getCurrentAdmin();
        if (admin == null || admin.getUniversity() == null) return "redirect:/login";

        if (!admin.getEmail().equals(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
            result.rejectValue("email", "error.admin", "Email already exists.");
        }

        if (result.hasErrors()) {
            model.addAttribute("university", admin.getUniversity());
            return "admin/university/admin-profile";
        }

        admin.setFirstName(request.getFirstName());
        admin.setLastName(request.getLastName());
        admin.setEmail(request.getEmail());
        admin.setUsername(request.getEmail());
        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            admin.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        userRepository.save(admin);
        redirectAttributes.addFlashAttribute("success", "Profile updated successfully.");
        return "redirect:/admin/university/profile";
    }

    // Notifications
    @GetMapping("/notifications")
    public String viewNotifications(Model model) {
        User admin = getCurrentAdmin();
        if (admin == null || admin.getUniversity() == null) return "redirect:/login";

        List<Notification> notifications = notificationRepository.findByUserId(admin.getId());

        model.addAttribute("university", admin.getUniversity());
        model.addAttribute("notifications", notifications);
        return "admin/university/university-notifications";
    }

    @PostMapping("/notifications/{id}/delete")
    public String deleteNotification(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        notificationRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("success", "Notification deleted.");
        return "redirect:/admin/university/notifications";
    }
}
