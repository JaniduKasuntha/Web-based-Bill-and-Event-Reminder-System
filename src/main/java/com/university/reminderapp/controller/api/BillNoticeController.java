package com.university.reminderapp.controller.api;

import com.university.reminderapp.dto.request.BillNoticeRequest;
import com.university.reminderapp.dto.response.ApiResponse;
import com.university.reminderapp.model.BillNotice;
import com.university.reminderapp.model.User;
import com.university.reminderapp.service.BillNoticeService;
import com.university.reminderapp.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/bill-notices")
public class BillNoticeController {
    @Autowired
    private BillNoticeService billNoticeService;

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<List<BillNotice>> getAllBillNotices() {
        User currentUser = userService.getCurrentUser();

        if (currentUser.getUniversity() != null) {
            return ResponseEntity.ok(billNoticeService.getBillNoticesByUniversity(currentUser.getUniversity()));
        }

        return ResponseEntity.ok(billNoticeService.getAllBillNotices());
    }

    @GetMapping("/active")
    public ResponseEntity<List<BillNotice>> getActiveBillNotices() {
        User currentUser = userService.getCurrentUser();

        if (currentUser.getUniversity() != null) {
            return ResponseEntity.ok(billNoticeService.getActiveBillNotices(currentUser.getUniversity().getId()));
        }

        return ResponseEntity.badRequest().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<BillNotice> getBillNoticeById(@PathVariable Long id) {
        return ResponseEntity.ok(billNoticeService.getBillNoticeById(id));
    }

    @PostMapping
    public ResponseEntity<BillNotice> createBillNotice(@Valid @RequestBody BillNoticeRequest request) {
        User currentUser = userService.getCurrentUser();

        if (currentUser.getRole().equals("UNIVERSITY_ADMIN")) {
            request.setUniversityId(currentUser.getUniversity().getId());
        }

        return new ResponseEntity<>(billNoticeService.createBillNotice(request, currentUser), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BillNotice> updateBillNotice(@PathVariable Long id, @Valid @RequestBody BillNoticeRequest request) {
        User currentUser = userService.getCurrentUser();
        return ResponseEntity.ok(billNoticeService.updateBillNotice(id, request, currentUser));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteBillNotice(@PathVariable Long id) {
        billNoticeService.deleteBillNotice(id);
        return ResponseEntity.ok(new ApiResponse(true, "Bill notice deleted successfully"));
    }
}