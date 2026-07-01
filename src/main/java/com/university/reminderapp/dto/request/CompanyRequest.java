package com.university.reminderapp.dto.request;

import lombok.Data;

@Data
public class CompanyRequest {
    private String name;
    private String description;
    private String status;
}
