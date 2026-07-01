package com.university.reminderapp.config;

import com.university.reminderapp.model.Company;
import com.university.reminderapp.model.University;
import com.university.reminderapp.model.User;
import com.university.reminderapp.repository.CompanyRepository;
import com.university.reminderapp.repository.UniversityRepository;
import com.university.reminderapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private UniversityRepository universityRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Seed Company
        Company company;
        if (companyRepository.count() == 0) {
            company = new Company();
            company.setName("Test Company");
            company.setDescription("Default Test Company");
            company.setStatus("ACTIVE");
            company = companyRepository.save(company);
        } else {
            company = companyRepository.findAll().get(0);
        }

        // Seed University
        University university;
        if (universityRepository.count() == 0) {
            university = new University();
            university.setName("Test University");
            university.setDescription("Default Test University");
            university.setAddress("123 University Ave");
            university.setContactEmail("contact@university.edu");
            university.setContactPhone("123-456-7890");
            university.setStatus("ACTIVE");
            university.setCompany(company);
            university = universityRepository.save(university);
        } else {
            university = universityRepository.findAll().get(0);
        }

        // Seed Users
        createUserIfNotExist("company_admin@example.com", "password123", "Company", "Admin", "ROLE_COMPANY_ADMIN", null, null);
        User parent = createUserIfNotExist("parent@example.com", "password123", "Parent", "User", "ROLE_PARENT", university, null);
        createUserIfNotExist("student@example.com", "password123", "Student", "User", "ROLE_STUDENT", university, parent);
        createUserIfNotExist("uni_admin@example.com", "password123", "University", "Admin", "ROLE_UNIVERSITY_ADMIN", university, null);
        createUserIfNotExist("event_manager@example.com", "password123", "Event", "Manager", "ROLE_EVENT_MANAGER", university, null);
    }

    private User createUserIfNotExist(String email, String password, String firstName, String lastName, String role, University university, User parent) {
        Optional<User> existingUser = userRepository.findByEmail(email);
        if (existingUser.isPresent()) {
            return existingUser.get();
        }

        User user = new User();
        user.setUsername(email);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setStatus("ACTIVE");
        user.setRoles(new HashSet<>(Collections.singletonList(role)));
        user.setUniversity(university);
        user.setParent(parent);

        return userRepository.save(user);
    }
}
