package com.university.reminderapp.dto.request;

import lombok.Data;

@Data
public class EmailHistoryRequest {
    private Long recipientId;
    private String subject;
    private String body;
}
