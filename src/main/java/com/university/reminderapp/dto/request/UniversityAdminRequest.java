package com.university.reminderapp.dto.request;

import lombok.Data;

@Data
public class UniversityAdminRequest {
    private Long universityId;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
}
