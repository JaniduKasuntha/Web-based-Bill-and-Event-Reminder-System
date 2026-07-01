package com.university.reminderapp.controller.api;

import com.university.reminderapp.model.Company;
import com.university.reminderapp.model.University;
import com.university.reminderapp.service.CompanyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class UniversityController {

    @Autowired
    private CompanyService companyService;

    @GetMapping("/companies/{companyId}/universities")
    public ResponseEntity<List<University>> getUniversitiesByCompany(@PathVariable("companyId") Long companyId) {
        Company company = companyService.getCompanyById(companyId);
        if (company == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(companyService.getUniversitiesByCompany(companyId));
    }

    @GetMapping("/universities/{id}")
    public ResponseEntity<University> getUniversityById(@PathVariable("id") Long id) {
        University university = companyService.getUniversityById(id);
        if (university == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(university);
    }

    @PostMapping("/companies/{companyId}/universities")
    public ResponseEntity<University> createUniversity(@PathVariable("companyId") Long companyId, @RequestBody University university) {
        Company company = companyService.getCompanyById(companyId);
        if (company == null) {
            return ResponseEntity.notFound().build();
        }
        university.setCompany(company);
        university.setStatus("ACTIVE");
        University saved = companyService.saveUniversity(university);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/universities/{id}")
    public ResponseEntity<University> updateUniversity(@PathVariable("id") Long id, @RequestBody University universityDetails) {
        University university = companyService.getUniversityById(id);
        if (university == null) {
            return ResponseEntity.notFound().build();
        }
        university.setName(universityDetails.getName());
        university.setDescription(universityDetails.getDescription());
        university.setAddress(universityDetails.getAddress());
        university.setContactEmail(universityDetails.getContactEmail());
        university.setContactPhone(universityDetails.getContactPhone());
        if (universityDetails.getStatus() != null) {
            university.setStatus(universityDetails.getStatus());
        }
        University updated = companyService.saveUniversity(university);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/universities/{id}")
    public ResponseEntity<Void> deleteUniversity(@PathVariable("id") Long id) {
        University university = companyService.getUniversityById(id);
        if (university == null) {
            return ResponseEntity.notFound().build();
        }
        companyService.deleteUniversity(id);
        return ResponseEntity.ok().build();
    }
}
