package com.university.reminderapp.service;

import com.university.reminderapp.model.Company;
import com.university.reminderapp.model.University;
import com.university.reminderapp.repository.CompanyRepository;
import com.university.reminderapp.repository.UniversityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CompanyService {

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private UniversityRepository universityRepository;

    public List<Company> getAllCompanies() {
        return companyRepository.findAll();
    }

    public Company getCompanyById(Long id) {
        return companyRepository.findById(id).orElse(null);
    }

    public Company saveCompany(Company company) {
        return companyRepository.save(company);
    }

    public void deleteCompany(Long id) {
        companyRepository.deleteById(id);
    }

    public List<University> getUniversitiesByCompany(Long companyId) {
        return universityRepository.findAllByCompanyId(companyId);
    }

    public University getUniversityById(Long id) {
        return universityRepository.findById(id).orElse(null);
    }

    public University saveUniversity(University university) {
        return universityRepository.save(university);
    }

    public void deleteUniversity(Long id) {
        universityRepository.deleteById(id);
    }
}
