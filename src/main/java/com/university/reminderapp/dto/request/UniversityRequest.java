package com.university.reminderapp.dto.request;

import lombok.Data;

@Data
public class UniversityRequest {
    private Long companyId;
    private String name;
    private String description;
    private String address;
    private String contactEmail;
    private String contactPhone;
    private String status;
}
