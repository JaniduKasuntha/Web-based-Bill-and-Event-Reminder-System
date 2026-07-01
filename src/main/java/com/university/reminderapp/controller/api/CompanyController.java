package com.university.reminderapp.controller.api;

import com.university.reminderapp.model.Company;
import com.university.reminderapp.service.CompanyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/companies")
public class CompanyController {

    @Autowired
    private CompanyService companyService;

    @GetMapping
    public List<Company> getAllCompanies() {
        return companyService.getAllCompanies();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Company> getCompanyById(@PathVariable("id") Long id) {
        Company company = companyService.getCompanyById(id);
        if (company == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(company);
    }

    @PostMapping
    public Company createCompany(@RequestBody Company company) {
        company.setStatus("ACTIVE");
        return companyService.saveCompany(company);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Company> updateCompany(@PathVariable("id") Long id, @RequestBody Company companyDetails) {
        Company company = companyService.getCompanyById(id);
        if (company == null) {
            return ResponseEntity.notFound().build();
        }
        company.setName(companyDetails.getName());
        company.setDescription(companyDetails.getDescription());
        if (companyDetails.getStatus() != null) {
            company.setStatus(companyDetails.getStatus());
        }
        Company updatedCompany = companyService.saveCompany(company);
        return ResponseEntity.ok(updatedCompany);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCompany(@PathVariable("id") Long id) {
        Company company = companyService.getCompanyById(id);
        if (company == null) {
            return ResponseEntity.notFound().build();
        }
        companyService.deleteCompany(id);
        return ResponseEntity.ok().build();
    }
}
