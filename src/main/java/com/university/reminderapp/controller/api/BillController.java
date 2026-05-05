package com.university.reminderapp.controller.api;

import com.university.reminderapp.dto.request.BillRequest;
import com.university.reminderapp.dto.response.ApiResponse;
import com.university.reminderapp.model.Bill;
import com.university.reminderapp.model.User;
import com.university.reminderapp.service.BillService;
import com.university.reminderapp.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.List;

@RestController
@RequestMapping("/api/bills")
public class BillController {
    @Autowired
    private BillService billService;

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<List<Bill>> getBills(@RequestParam(required = false) Boolean me) {
        User currentUser = userService.getCurrentUser();

        if (Boolean.TRUE.equals(me) && currentUser.getRole().equals("STUDENT")) {
            return ResponseEntity.ok(billService.getBillsByStudent(currentUser));
        } else if (Boolean.TRUE.equals(me) && currentUser.getRole().equals("PARENT")) {
            return ResponseEntity.ok(billService.getBillsByParent(currentUser));
        } else if (currentUser.getRole().equals("UNIVERSITY_ADMIN")) {
            return ResponseEntity.ok(billService.getBillsByUniversity(currentUser.getUniversity().getId()));
        }

        return ResponseEntity.ok(billService.getAllBills());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Bill> getBillById(@PathVariable Long id) {
        return ResponseEntity.ok(billService.getBillById(id));
    }

    @PostMapping
    public ResponseEntity<Bill> createBill(@Valid @RequestBody BillRequest request) {
        User currentUser = userService.getCurrentUser();

        if (currentUser.getRole().equals("STUDENT")) {
            request.setStudentId(currentUser.getId());
            request.setUniversityId(currentUser.getUniversity().getId());
        }

        return new ResponseEntity<>(billService.createBill(request, currentUser), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Bill> updateBill(@PathVariable Long id, @Valid @RequestBody BillRequest request) {
        User currentUser = userService.getCurrentUser();
        return ResponseEntity.ok(billService.updateBill(id, request, currentUser));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteBill(@PathVariable Long id) {
        User currentUser = userService.getCurrentUser();
        billService.deleteBill(id, currentUser);
        return ResponseEntity.ok(new ApiResponse(true, "Bill deleted successfully"));
    }

    @PatchMapping("/{id}/accept")
    public ResponseEntity<Bill> acceptBill(@PathVariable Long id) {
        User currentUser = userService.getCurrentUser();
        return ResponseEntity.ok(billService.acceptBill(id, currentUser));
    }
}

