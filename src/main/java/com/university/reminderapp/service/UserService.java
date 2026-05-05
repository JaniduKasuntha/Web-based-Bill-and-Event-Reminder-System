package com.university.reminderapp.service;

import com.university.reminderapp.model.User;
import com.university.reminderapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username;
        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        } else {
            username = principal.toString();
        }

        return userRepository.findByUsername(username).orElse(null);
    }

    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public List<User> getUniversityAdmins(Long universityId) {
        return userRepository.findAllByUniversityIdAndRolesContaining(universityId, "UNIVERSITY_ADMIN");
    }

    public List<User> getStudentsByUniversity(Long universityId) {
        return userRepository.findAllByUniversityIdAndRolesContaining(universityId, "STUDENT");
    }
}


