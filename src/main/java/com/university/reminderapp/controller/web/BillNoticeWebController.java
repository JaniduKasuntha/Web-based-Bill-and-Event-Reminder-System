package com.university.reminderapp.controller.web;


import com.university.reminderapp.dto.request.BillNoticeRequest;
import com.university.reminderapp.model.BillNotice;
import com.university.reminderapp.model.University;
import com.university.reminderapp.model.User;
import com.university.reminderapp.service.BillNoticeService;
import com.university.reminderapp.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
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
@RequestMapping("/admin/bill-notices")
@PreAuthorize("hasRole('UNIVERSITY_ADMIN')")
public class BillNoticeWebController {

    @Autowired
    private BillNoticeService billNoticeService;

    @Autowired
    private UserService userService;

    @GetMapping
    public String listBillNotices(@RequestParam(required = false) String active,
                                  @RequestParam(required = false) String search,
                                  Model model) {
        User currentUser = userService.getCurrentUser();
        University university = currentUser.getUniversity();

        List<BillNotice> notices = billNoticeService.getBillNoticesByUniversity(university);

        if (active != null && !active.isBlank()) {
            LocalDate today = LocalDate.now();
            if ("true".equalsIgnoreCase(active)) {
                notices = notices.stream()
                        .filter(notice -> !today.isBefore(notice.getValidFrom()) && !today.isAfter(notice.getValidTo()))
                        .collect(Collectors.toList());
            } else if ("false".equalsIgnoreCase(active)) {
                notices = notices.stream()
                        .filter(notice -> today.isBefore(notice.getValidFrom()) || today.isAfter(notice.getValidTo()))
                        .collect(Collectors.toList());
            }
        }

        if (search != null && !search.isBlank()) {
            String term = search.toLowerCase();
            notices = notices.stream()
                    .filter(notice ->
                            (notice.getTitle() != null && notice.getTitle().toLowerCase().contains(term)) ||
                                    (notice.getMessage() != null && notice.getMessage().toLowerCase().contains(term)))
                    .collect(Collectors.toList());
        }

        model.addAttribute("notices", notices);
        model.addAttribute("university", university);
        return "bill/bill-notice-list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        User currentUser = userService.getCurrentUser();
        BillNoticeRequest request = new BillNoticeRequest();
        request.setUniversityId(currentUser.getUniversity().getId());

        model.addAttribute("notice", request);
        model.addAttribute("noticeId", null);
        return "bill/bill-notice-form";
    }

    @PostMapping("/create")
    public String createBillNotice(@Valid @ModelAttribute("notice") BillNoticeRequest request,
                                   BindingResult result,
                                   RedirectAttributes redirectAttributes,
                                   Model model) {
        User currentUser = userService.getCurrentUser();
        request.setUniversityId(currentUser.getUniversity().getId());

        if (result.hasErrors()) {
            model.addAttribute("noticeId", null);
            return "bill/bill-notice-form";
        }

        billNoticeService.createBillNotice(request, currentUser);
        redirectAttributes.addFlashAttribute("success", "Bill notice created successfully!");
        return "redirect:/admin/bill-notices";
    }

    @GetMapping("/{id}")
    public String viewBillNotice(@PathVariable Long id, Model model) {
        BillNotice notice = billNoticeService.getBillNoticeById(id);
        model.addAttribute("notice", notice);
        return "bill/bill-notice-detail";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        BillNotice notice = billNoticeService.getBillNoticeById(id);

        BillNoticeRequest request = new BillNoticeRequest();
        request.setUniversityId(notice.getUniversity().getId());
        request.setTitle(notice.getTitle());
        request.setMessage(notice.getMessage());
        request.setValidFrom(notice.getValidFrom());
        request.setValidTo(notice.getValidTo());

        model.addAttribute("notice", request);
        model.addAttribute("noticeId", id);
        return "bill/bill-notice-form";
    }

    @PostMapping("/{id}/edit")
    public String updateBillNotice(@PathVariable Long id,
                                   @Valid @ModelAttribute("notice") BillNoticeRequest request,
                                   BindingResult result,
                                   RedirectAttributes redirectAttributes,
                                   Model model) {
        User currentUser = userService.getCurrentUser();

        if (result.hasErrors()) {
            model.addAttribute("noticeId", id);
            return "bill/bill-notice-form";
        }

        billNoticeService.updateBillNotice(id, request, currentUser);
        redirectAttributes.addFlashAttribute("success", "Bill notice updated successfully!");
        return "redirect:/admin/bill-notices";
    }

    @PostMapping("/{id}/delete")
    public String deleteBillNotice(@PathVariable Long id,
                                   RedirectAttributes redirectAttributes) {
        billNoticeService.deleteBillNotice(id);
        redirectAttributes.addFlashAttribute("success", "Bill notice deleted successfully!");
        return "redirect:/admin/bill-notices";
    }
}
