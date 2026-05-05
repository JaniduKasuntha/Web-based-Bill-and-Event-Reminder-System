package com.university.reminderapp.controller.web;


import com.university.reminderapp.dto.request.BillRequest;
import com.university.reminderapp.exception.AccessDeniedException;
import com.university.reminderapp.model.Bill;
import com.university.reminderapp.model.User;
import com.university.reminderapp.service.BillService;
import com.university.reminderapp.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/bills")
public class BillWebController {

    @Autowired
    private BillService billService;

    @Autowired
    private UserService userService;

    @GetMapping
    public String listBills(@RequestParam(required = false) String status,
                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDateBefore,
                            @RequestParam(required = false) Long studentId,
                            @RequestParam(required = false, name = "me") Boolean me,
                            Model model) {
        User currentUser = userService.getCurrentUser();
        String role = currentUser.getRole();

        List<Bill> bills;

        if ("STUDENT".equals(role) || Boolean.TRUE.equals(me)) {
            bills = billService.getBillsByStudent(currentUser);
        } else if ("PARENT".equals(role)) {
            bills = billService.getBillsByParent(currentUser);
        } else if ("UNIVERSITY_ADMIN".equals(role)) {
            bills = billService.getBillsByUniversity(currentUser.getUniversity().getId());
        } else {
            bills = billService.getAllBills();
        }

        if (studentId != null) {
            bills = bills.stream()
                    .filter(bill -> bill.getStudent() != null && bill.getStudent().getId().equals(studentId))
                    .collect(Collectors.toList());
            model.addAttribute("filteredStudentId", studentId);
        }

        if (status != null && !status.isBlank()) {
            bills = bills.stream()
                    .filter(bill -> status.equalsIgnoreCase(bill.getStatus()))
                    .collect(Collectors.toList());
        }

        if (dueDateBefore != null) {
            bills = bills.stream()
                    .filter(bill -> bill.getDueDate() != null && !bill.getDueDate().isAfter(dueDateBefore))
                    .collect(Collectors.toList());
        }

        model.addAttribute("bills", bills);
        model.addAttribute("status", status);
        model.addAttribute("dueDateBefore", dueDateBefore);

        if ("UNIVERSITY_ADMIN".equals(role)) {
            model.addAttribute("students", userService.getStudentsByUniversity(currentUser.getUniversity().getId()));
        }

        return "bill/bill-list";
    }

    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        User currentUser = userService.getCurrentUser();
        BillRequest request = new BillRequest();
        request.setUniversityId(currentUser.getUniversity().getId());
        request.setStudentId(currentUser.getId());

        model.addAttribute("bill", request);
        model.addAttribute("billId", null);
        model.addAttribute("student", currentUser);
        model.addAttribute("editableUntil", null);
        return "bill/bill-form";
    }

    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping("/create")
    public String createBill(@Valid @ModelAttribute("bill") BillRequest request,
                             BindingResult result,
                             RedirectAttributes redirectAttributes,
                             Model model) {
        User currentUser = userService.getCurrentUser();
        request.setUniversityId(currentUser.getUniversity().getId());
        request.setStudentId(currentUser.getId());

        if (result.hasErrors()) {
            model.addAttribute("billId", null);
            model.addAttribute("student", currentUser);
            model.addAttribute("editableUntil", null);
            return "bill/bill-form";
        }

        billService.createBill(request, currentUser);
        redirectAttributes.addFlashAttribute("success", "Bill created successfully!");
        return "redirect:/bills?me=true";
    }

    @GetMapping("/{id}")
    public String viewBill(@PathVariable Long id, Model model) {
        Bill bill = billService.getBillById(id);
        User currentUser = userService.getCurrentUser();
        ensureVisibility(bill, currentUser);

        model.addAttribute("bill", bill);
        model.addAttribute("student", bill.getStudent());
        return "bill/bill-detail";
    }

    @PreAuthorize("hasAnyRole('STUDENT','UNIVERSITY_ADMIN')")
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        Bill bill = billService.getBillById(id);
        User currentUser = userService.getCurrentUser();
        ensureEditAccess(bill, currentUser);

        BillRequest request = new BillRequest();
        request.setUniversityId(bill.getUniversity().getId());
        request.setStudentId(bill.getStudent().getId());
        request.setParentId(bill.getParent() != null ? bill.getParent().getId() : null);
        request.setAmount(bill.getAmount());
        request.setDescription(bill.getDescription());
        request.setDueDate(bill.getDueDate());

        model.addAttribute("bill", request);
        model.addAttribute("billId", bill.getId());
        model.addAttribute("student", bill.getStudent());
        model.addAttribute("editableUntil", bill.getEditableUntil());
        return "bill/bill-form";
    }

    @PreAuthorize("hasAnyRole('STUDENT','UNIVERSITY_ADMIN')")
    @PostMapping("/{id}/edit")
    public String updateBill(@PathVariable Long id,
                             @Valid @ModelAttribute("bill") BillRequest request,
                             BindingResult result,
                             RedirectAttributes redirectAttributes,
                             Model model) {
        Bill bill = billService.getBillById(id);
        User currentUser = userService.getCurrentUser();
        ensureEditAccess(bill, currentUser);

        if ("STUDENT".equals(currentUser.getRole())) {
            request.setUniversityId(currentUser.getUniversity().getId());
            request.setStudentId(currentUser.getId());
        }

        if (result.hasErrors()) {
            model.addAttribute("billId", id);
            model.addAttribute("student", bill.getStudent());
            model.addAttribute("editableUntil", bill.getEditableUntil());
            return "bill/bill-form";
        }

        billService.updateBill(id, request, currentUser);
        redirectAttributes.addFlashAttribute("success", "Bill updated successfully!");
        return "redirect:/bills/" + id;
    }

    @PreAuthorize("hasAnyRole('STUDENT','UNIVERSITY_ADMIN')")
    @PostMapping("/{id}/delete")
    public String deleteBill(@PathVariable Long id,
                             RedirectAttributes redirectAttributes) {
        User currentUser = userService.getCurrentUser();
        billService.deleteBill(id, currentUser);
        redirectAttributes.addFlashAttribute("success", "Bill deleted successfully!");
        return "redirect:/bills" + ("STUDENT".equals(currentUser.getRole()) ? "?me=true" : "");
    }

    @PreAuthorize("hasRole('UNIVERSITY_ADMIN')")
    @PostMapping("/{id}/accept")
    public String acceptBill(@PathVariable Long id,
                             RedirectAttributes redirectAttributes) {
        User currentUser = userService.getCurrentUser();
        billService.acceptBill(id, currentUser);
        redirectAttributes.addFlashAttribute("success", "Bill accepted successfully!");
        return "redirect:/bills";
    }

    private void ensureVisibility(Bill bill, User currentUser) {
        String role = currentUser.getRole();
        if ("STUDENT".equals(role) && !bill.getStudent().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You can only view your own bills");
        }
        if ("PARENT".equals(role) && (bill.getParent() == null || !bill.getParent().getId().equals(currentUser.getId()))) {
            throw new AccessDeniedException("You can only view bills linked to your student");
        }
        if ("UNIVERSITY_ADMIN".equals(role) && !bill.getUniversity().getId().equals(currentUser.getUniversity().getId())) {
            throw new AccessDeniedException("You can only view bills from your university");
        }
    }

    private void ensureEditAccess(Bill bill, User currentUser) {
        if ("STUDENT".equals(currentUser.getRole()) && !bill.getStudent().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You can only edit your own bills");
        }
        if ("UNIVERSITY_ADMIN".equals(currentUser.getRole()) &&
                !bill.getUniversity().getId().equals(currentUser.getUniversity().getId())) {
            throw new AccessDeniedException("You can only edit bills from your university");
        }
    }
}
