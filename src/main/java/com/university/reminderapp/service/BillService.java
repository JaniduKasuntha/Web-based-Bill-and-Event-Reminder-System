package com.university.reminderapp.service;


import com.university.reminderapp.dto.request.BillRequest;
import com.university.reminderapp.exception.AccessDeniedException;
import com.university.reminderapp.exception.ResourceNotFoundException;
import com.university.reminderapp.model.Bill;
import com.university.reminderapp.model.University;
import com.university.reminderapp.model.User;
import com.university.reminderapp.repository.BillRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BillService {
    @Autowired
    private BillRepository billRepository;

    @Autowired
    private UniversityService universityService;

    @Autowired
    private UserService userService;

    @Autowired
    private NotificationService notificationService;

    public List<Bill> getAllBills() {
        return billRepository.findAll();
    }

    public List<Bill> getBillsByUniversity(Long universityId) {
        return billRepository.findByUniversityId(universityId);
    }

    public List<Bill> getBillsByStudent(User student) {
        return billRepository.findByStudent(student);
    }

    public List<Bill> getBillsByParent(User parent) {
        return billRepository.findByParent(parent);
    }

    public Bill getBillById(Long id) {
        return billRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found with id: " + id));
    }

    public Bill createBill(BillRequest request, User currentUser) {
        University university = universityService.getUniversityById(request.getUniversityId());

        User student;
        if (currentUser.getRole().equals("STUDENT")) {
            student = currentUser;
        } else {
            // Admin creating bill for a student
            student = userService.getUserById(request.getStudentId());
        }

        User parent = null;
        if (request.getParentId() != null) {
            parent = userService.getUserById(request.getParentId());
        }

        Bill bill = new Bill();
        bill.setUniversity(university);
        bill.setStudent(student);
        bill.setParent(parent);
        bill.setAmount(request.getAmount());
        bill.setDescription(request.getDescription());
        bill.setDueDate(request.getDueDate());
        bill.setStatus("PENDING");
        bill.setCreatedBy(currentUser);
        bill.setUpdatedBy(currentUser);

        Bill savedBill = billRepository.save(bill);

        // Send notifications to University Admin and Parent
        if (parent != null) {
            notificationService.sendBillNotification(parent, savedBill, "New bill added");
        }

        // Notify university admins
        List<User> universityAdmins = userService.getUniversityAdmins(university.getId());
        for (User admin : universityAdmins) {
            notificationService.sendBillNotification(admin, savedBill, "New student bill requires review");
        }

        return savedBill;
    }

    public Bill updateBill(Long id, BillRequest request, User currentUser) {
        Bill bill = getBillById(id);

        // Check if current time is before editable_until
        if (LocalDateTime.now().isAfter(bill.getEditableUntil())) {
            throw new AccessDeniedException("Bill can only be edited within 48 hours of creation");
        }

        // Check if the current user is the bill owner or admin
        if (currentUser.getRole().equals("STUDENT") && !bill.getStudent().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You can only edit your own bills");
        }

        bill.setAmount(request.getAmount());
        bill.setDescription(request.getDescription());
        bill.setDueDate(request.getDueDate());

        // Update parent if provided and different
        if (request.getParentId() != null &&
                (bill.getParent() == null || !bill.getParent().getId().equals(request.getParentId()))) {
            User parent = userService.getUserById(request.getParentId());
            bill.setParent(parent);

            // Notify new parent
            notificationService.sendBillNotification(parent, bill, "You have been linked to a bill");
        }

        bill.setUpdatedBy(currentUser);

        return billRepository.save(bill);
    }

    public Bill acceptBill(Long id, User currentUser) {
        Bill bill = getBillById(id);

        // Only university admin can accept bills
        if (!currentUser.getRole().equals("UNIVERSITY_ADMIN")) {
            throw new AccessDeniedException("Only university admins can accept bills");
        }

        bill.setStatus("ACCEPTED");
        bill.setUpdatedBy(currentUser);

        Bill savedBill = billRepository.save(bill);

        // Notify student
        notificationService.sendBillNotification(bill.getStudent(), savedBill, "Your bill has been accepted");

        // Notify parent if exists
        if (bill.getParent() != null) {
            notificationService.sendBillNotification(bill.getParent(), savedBill, "Student bill has been accepted");
        }

        return savedBill;
    }

    public void deleteBill(Long id, User currentUser) {
        Bill bill = getBillById(id);

        // Students can only delete their own bills
        if (currentUser.getRole().equals("STUDENT") && !bill.getStudent().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You can only delete your own bills");
        }

        billRepository.delete(bill);
    }
}
