package com.university.reminderapp.controller.web;

import com.university.reminderapp.dto.request.CompanyRequest;
import com.university.reminderapp.dto.request.UniversityAdminRequest;
import com.university.reminderapp.dto.request.UniversityRequest;
import com.university.reminderapp.model.Company;
import com.university.reminderapp.model.University;
import com.university.reminderapp.model.User;
import com.university.reminderapp.repository.UserRepository;
import com.university.reminderapp.service.CompanyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class CompanyAdminController {

    @Autowired
    private CompanyService companyService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Companies Mappings
    @GetMapping("/companies")
    public String listCompanies(Model model) {
        model.addAttribute("companies", companyService.getAllCompanies());
        return "admin/company/company-list";
    }

    @GetMapping("/companies/create")
    public String showCreateCompanyForm(Model model) {
        model.addAttribute("company", new CompanyRequest());
        model.addAttribute("companyId", null);
        return "admin/company/company-form";
    }

    @PostMapping("/companies/create")
    public String createCompany(@ModelAttribute("company") CompanyRequest request, BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "admin/company/company-form";
        }
        Company company = new Company();
        company.setName(request.getName());
        company.setDescription(request.getDescription());
        company.setStatus("ACTIVE");
        companyService.saveCompany(company);
        redirectAttributes.addFlashAttribute("success", "Company created successfully.");
        return "redirect:/admin/companies";
    }

    @GetMapping("/companies/edit/{id}")
    public String showEditCompanyForm(@PathVariable("id") Long id, Model model) {
        Company company = companyService.getCompanyById(id);
        if (company == null) {
            model.addAttribute("error", "Company not found.");
            return "error/404";
        }
        CompanyRequest request = new CompanyRequest();
        request.setName(company.getName());
        request.setDescription(company.getDescription());
        request.setStatus(company.getStatus());

        model.addAttribute("company", request);
        model.addAttribute("companyId", id);
        return "admin/company/company-form";
    }

    @PostMapping("/companies/edit/{id}")
    public String editCompany(@PathVariable("id") Long id, @ModelAttribute("company") CompanyRequest request, BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "admin/company/company-form";
        }
        Company company = companyService.getCompanyById(id);
        if (company == null) {
            return "redirect:/admin/companies";
        }
        company.setName(request.getName());
        company.setDescription(request.getDescription());
        if (request.getStatus() != null) {
            company.setStatus(request.getStatus());
        }
        companyService.saveCompany(company);
        redirectAttributes.addFlashAttribute("success", "Company updated successfully.");
        return "redirect:/admin/companies";
    }

    @PostMapping("/companies/{id}/delete")
    public String deleteCompany(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        companyService.deleteCompany(id);
        redirectAttributes.addFlashAttribute("success", "Company deleted successfully.");
        return "redirect:/admin/companies";
    }

    @PostMapping("/companies/{id}/deactivate")
    public String deactivateCompany(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        Company company = companyService.getCompanyById(id);
        if (company != null) {
            company.setStatus("INACTIVE");
            companyService.saveCompany(company);
            redirectAttributes.addFlashAttribute("success", "Company deactivated successfully.");
        }
        return "redirect:/admin/companies";
    }

    @PostMapping("/companies/{id}/activate")
    public String activateCompany(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        Company company = companyService.getCompanyById(id);
        if (company != null) {
            company.setStatus("ACTIVE");
            companyService.saveCompany(company);
            redirectAttributes.addFlashAttribute("success", "Company activated successfully.");
        }
        return "redirect:/admin/companies";
    }

    // Universities Mappings
    @GetMapping("/companies/{companyId}/universities")
    public String listUniversities(@PathVariable("companyId") Long companyId, Model model) {
        Company company = companyService.getCompanyById(companyId);
        if (company == null) {
            model.addAttribute("error", "Company not found.");
            return "error/404";
        }
        model.addAttribute("company", company);
        model.addAttribute("universities", companyService.getUniversitiesByCompany(companyId));
        return "admin/university/university-list";
    }

    @GetMapping("/companies/{companyId}/universities/create")
    public String showCreateUniversityForm(@PathVariable("companyId") Long companyId, Model model) {
        Company company = companyService.getCompanyById(companyId);
        if (company == null) {
            model.addAttribute("error", "Company not found.");
            return "error/404";
        }
        UniversityRequest request = new UniversityRequest();
        request.setCompanyId(companyId);

        model.addAttribute("company", company);
        model.addAttribute("university", request);
        model.addAttribute("universityId", null);
        return "admin/university/university-form";
    }

    @PostMapping("/companies/{companyId}/universities/create")
    public String createUniversity(@PathVariable("companyId") Long companyId, @ModelAttribute("university") UniversityRequest request, BindingResult result, RedirectAttributes redirectAttributes, Model model) {
        Company company = companyService.getCompanyById(companyId);
        if (company == null) {
            model.addAttribute("error", "Company not found.");
            return "error/404";
        }
        if (result.hasErrors()) {
            model.addAttribute("company", company);
            return "admin/university/university-form";
        }
        University university = new University();
        university.setName(request.getName());
        university.setDescription(request.getDescription());
        university.setAddress(request.getAddress());
        university.setContactEmail(request.getContactEmail());
        university.setContactPhone(request.getContactPhone());
        university.setStatus("ACTIVE");
        university.setCompany(company);
        companyService.saveUniversity(university);

        redirectAttributes.addFlashAttribute("success", "University created successfully.");
        return "redirect:/admin/companies/" + companyId + "/universities";
    }

    @GetMapping("/companies/{companyId}/universities/{id}/edit")
    public String showEditUniversityForm(@PathVariable("companyId") Long companyId, @PathVariable("id") Long id, Model model) {
        Company company = companyService.getCompanyById(companyId);
        University university = companyService.getUniversityById(id);
        if (company == null || university == null) {
            model.addAttribute("error", "Not found.");
            return "error/404";
        }
        UniversityRequest request = new UniversityRequest();
        request.setName(university.getName());
        request.setDescription(university.getDescription());
        request.setAddress(university.getAddress());
        request.setContactEmail(university.getContactEmail());
        request.setContactPhone(university.getContactPhone());
        request.setStatus(university.getStatus());
        request.setCompanyId(companyId);

        model.addAttribute("company", company);
        model.addAttribute("university", request);
        model.addAttribute("universityId", id);
        return "admin/university/university-form";
    }

    @PostMapping("/companies/{companyId}/universities/{id}/edit")
    public String editUniversity(@PathVariable("companyId") Long companyId, @PathVariable("id") Long id, @ModelAttribute("university") UniversityRequest request, BindingResult result, RedirectAttributes redirectAttributes, Model model) {
        Company company = companyService.getCompanyById(companyId);
        University university = companyService.getUniversityById(id);
        if (company == null || university == null) {
            return "redirect:/admin/companies";
        }
        if (result.hasErrors()) {
            model.addAttribute("company", company);
            model.addAttribute("universityId", id);
            return "admin/university/university-form";
        }
        university.setName(request.getName());
        university.setDescription(request.getDescription());
        university.setAddress(request.getAddress());
        university.setContactEmail(request.getContactEmail());
        university.setContactPhone(request.getContactPhone());
        if (request.getStatus() != null) {
            university.setStatus(request.getStatus());
        }
        companyService.saveUniversity(university);

        redirectAttributes.addFlashAttribute("success", "University updated successfully.");
        return "redirect:/admin/companies/" + companyId + "/universities";
    }

    @PostMapping("/companies/{companyId}/universities/{id}/delete")
    public String deleteUniversity(@PathVariable("companyId") Long companyId, @PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        companyService.deleteUniversity(id);
        redirectAttributes.addFlashAttribute("success", "University deleted successfully.");
        return "redirect:/admin/companies/" + companyId + "/universities";
    }

    // University Admins Mappings
    @GetMapping("/companies/{companyId}/universities/{id}/admins")
    public String listUniversityAdmins(@PathVariable("companyId") Long companyId, @PathVariable("id") Long id, Model model) {
        Company company = companyService.getCompanyById(companyId);
        University university = companyService.getUniversityById(id);
        if (company == null || university == null) {
            model.addAttribute("error", "Not found.");
            return "error/404";
        }

        List<User> admins = userRepository.findAllByUniversityId(id).stream()
                .filter(u -> u.getRoles() != null && u.getRoles().contains("ROLE_UNIVERSITY_ADMIN"))
                .collect(Collectors.toList());

        model.addAttribute("company", company);
        model.addAttribute("university", university);
        model.addAttribute("admins", admins);
        return "admin/university/university-admin-list";
    }

    @GetMapping("/companies/{companyId}/universities/{id}/admins/create")
    public String showCreateAdminForm(@PathVariable("companyId") Long companyId, @PathVariable("id") Long id, Model model) {
        Company company = companyService.getCompanyById(companyId);
        University university = companyService.getUniversityById(id);
        if (company == null || university == null) {
            model.addAttribute("error", "Not found.");
            return "error/404";
        }
        UniversityAdminRequest request = new UniversityAdminRequest();
        request.setUniversityId(id);

        model.addAttribute("company", company);
        model.addAttribute("university", university);
        model.addAttribute("admin", request);
        model.addAttribute("adminId", null);
        return "admin/university/admin-form";
    }

    @PostMapping("/companies/{companyId}/universities/{id}/admins/create")
    public String createUniversityAdmin(@PathVariable("companyId") Long companyId, @PathVariable("id") Long id, @ModelAttribute("admin") UniversityAdminRequest request, BindingResult result, RedirectAttributes redirectAttributes, Model model) {
        Company company = companyService.getCompanyById(companyId);
        University university = companyService.getUniversityById(id);
        if (company == null || university == null) {
            return "redirect:/admin/companies";
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            result.rejectValue("email", "error.admin", "An account with this email already exists.");
        }
        if (result.hasErrors()) {
            model.addAttribute("company", company);
            model.addAttribute("university", university);
            return "admin/university/admin-form";
        }

        User user = new User();
        user.setUsername(request.getEmail());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setStatus("ACTIVE");
        user.setRoles(new HashSet<>(Collections.singletonList("ROLE_UNIVERSITY_ADMIN")));
        user.setUniversity(university);
        userRepository.save(user);

        redirectAttributes.addFlashAttribute("success", "University Admin created successfully.");
        return "redirect:/admin/companies/" + companyId + "/universities/" + id + "/admins";
    }

    @GetMapping("/companies/{companyId}/universities/{universityId}/admins/{id}/edit")
    public String showEditAdminForm(@PathVariable("companyId") Long companyId, @PathVariable("universityId") Long universityId, @PathVariable("id") Long id, Model model) {
        Company company = companyService.getCompanyById(companyId);
        University university = companyService.getUniversityById(universityId);
        User user = userRepository.findById(id).orElse(null);
        if (company == null || university == null || user == null) {
            model.addAttribute("error", "Not found.");
            return "error/404";
        }
        UniversityAdminRequest request = new UniversityAdminRequest();
        request.setFirstName(user.getFirstName());
        request.setLastName(user.getLastName());
        request.setEmail(user.getEmail());
        request.setUniversityId(universityId);

        model.addAttribute("company", company);
        model.addAttribute("university", university);
        model.addAttribute("admin", request);
        model.addAttribute("adminId", id);
        return "admin/university/admin-form";
    }

    @PostMapping("/companies/{companyId}/universities/{universityId}/admins/{id}/edit")
    public String editUniversityAdmin(@PathVariable("companyId") Long companyId, @PathVariable("universityId") Long universityId, @PathVariable("id") Long id, @ModelAttribute("admin") UniversityAdminRequest request, BindingResult result, RedirectAttributes redirectAttributes, Model model) {
        Company company = companyService.getCompanyById(companyId);
        University university = companyService.getUniversityById(universityId);
        User user = userRepository.findById(id).orElse(null);
        if (company == null || university == null || user == null) {
            return "redirect:/admin/companies";
        }
        if (!user.getEmail().equals(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
            result.rejectValue("email", "error.admin", "An account with this email already exists.");
        }
        if (result.hasErrors()) {
            model.addAttribute("company", company);
            model.addAttribute("university", university);
            model.addAttribute("adminId", id);
            return "admin/university/admin-form";
        }
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setUsername(request.getEmail());
        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        userRepository.save(user);

        redirectAttributes.addFlashAttribute("success", "University Admin updated successfully.");
        return "redirect:/admin/companies/" + companyId + "/universities/" + universityId + "/admins";
    }

    @PostMapping("/companies/{companyId}/universities/{universityId}/admins/{id}/delete")
    public String deleteUniversityAdmin(@PathVariable("companyId") Long companyId, @PathVariable("universityId") Long universityId, @PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        userRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("success", "University Admin deleted successfully.");
        return "redirect:/admin/companies/" + companyId + "/universities/" + universityId + "/admins";
    }
}
