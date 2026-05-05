package com.university.reminderapp.repository;


import com.university.reminderapp.model.Bill;
import com.university.reminderapp.model.University;
import com.university.reminderapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {
    List<Bill> findByUniversityAndStatus(University university, String status);
    List<Bill> findByStudent(User student);
    List<Bill> findByParent(User parent);
    List<Bill> findByUniversityId(Long universityId);
    void deleteByStudent(User student);
}