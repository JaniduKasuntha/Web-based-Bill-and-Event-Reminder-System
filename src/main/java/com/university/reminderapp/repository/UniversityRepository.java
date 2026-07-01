package com.university.reminderapp.repository;

import com.university.reminderapp.model.University;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UniversityRepository extends JpaRepository<University, Long> {
    java.util.List<University> findAllByCompanyId(Long companyId);
}

