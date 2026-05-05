package com.university.reminderapp.service;

import com.university.reminderapp.model.University;
import com.university.reminderapp.repository.UniversityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UniversityService {

    @Autowired
    private UniversityRepository universityRepository;

    public University getUniversityById(Long id) {
        return universityRepository.findById(id).orElse(null);
    }
}

