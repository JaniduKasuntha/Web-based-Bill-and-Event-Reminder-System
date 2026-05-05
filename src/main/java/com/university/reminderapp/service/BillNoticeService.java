package com.university.reminderapp.service;


import com.university.reminderapp.dto.request.BillNoticeRequest;
import com.university.reminderapp.exception.ResourceNotFoundException;
import com.university.reminderapp.model.BillNotice;
import com.university.reminderapp.model.University;
import com.university.reminderapp.model.User;
import com.university.reminderapp.repository.BillNoticeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class BillNoticeService {
    @Autowired
    private BillNoticeRepository billNoticeRepository;

    @Autowired
    private UniversityService universityService;

    @Autowired
    private UserService userService;

    @Autowired
    private NotificationService notificationService;

    public List<BillNotice> getAllBillNotices() {
        return billNoticeRepository.findAll();
    }

    public List<BillNotice> getBillNoticesByUniversity(University university) {
        return billNoticeRepository.findByUniversity(university);
    }

    public List<BillNotice> getActiveBillNotices(Long universityId) {
        return billNoticeRepository.findActiveNotices(universityId, LocalDate.now());
    }

    public BillNotice getBillNoticeById(Long id) {
        return billNoticeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bill notice not found with id: " + id));
    }

    public BillNotice createBillNotice(BillNoticeRequest request, User currentUser) {
        University university = universityService.getUniversityById(request.getUniversityId());

        BillNotice notice = new BillNotice();
        notice.setUniversity(university);
        notice.setTitle(request.getTitle());
        notice.setMessage(request.getMessage());
        notice.setValidFrom(request.getValidFrom());
        notice.setValidTo(request.getValidTo());
        notice.setCreatedBy(currentUser);
        notice.setUpdatedBy(currentUser);

        BillNotice savedNotice = billNoticeRepository.save(notice);

        // Send notifications to all students and parents of the university
        List<User> students = userService.getStudentsByUniversity(university.getId());
        for (User student : students) {
            notificationService.sendBillNoticeNotification(student, savedNotice);

            // Also notify parent if exists
            if (student.getParent() != null) {
                notificationService.sendBillNoticeNotification(student.getParent(), savedNotice);
            }
        }

        return savedNotice;
    }

    public BillNotice updateBillNotice(Long id, BillNoticeRequest request, User currentUser) {
        BillNotice notice = getBillNoticeById(id);

        notice.setTitle(request.getTitle());
        notice.setMessage(request.getMessage());
        notice.setValidFrom(request.getValidFrom());
        notice.setValidTo(request.getValidTo());
        notice.setUpdatedBy(currentUser);

        return billNoticeRepository.save(notice);
    }

    public void deleteBillNotice(Long id) {
        BillNotice notice = getBillNoticeById(id);
        billNoticeRepository.delete(notice);
    }
}