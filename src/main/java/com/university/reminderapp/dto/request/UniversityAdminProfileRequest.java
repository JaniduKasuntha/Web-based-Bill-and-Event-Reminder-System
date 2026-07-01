package com.university.reminderapp.dto.request;

import lombok.Data;

@Data
public class UniversityAdminProfileRequest {
    private String firstName;
    private String lastName;
    private String email;
    private String password;
}
