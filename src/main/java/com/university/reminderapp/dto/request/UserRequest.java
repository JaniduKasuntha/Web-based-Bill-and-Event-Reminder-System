package com.university.reminderapp.dto.request;

import lombok.Data;

@Data
public class UserRequest {
    private Long universityId;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String role;
    private Long parentId;
    private String status;
}
