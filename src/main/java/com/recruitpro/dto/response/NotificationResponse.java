package com.recruitpro.dto.response;

import com.recruitpro.enums.NotificationType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NotificationResponse {

    private Long id;
    private NotificationType type;
    private String title;
    private String message;
    private boolean read;
    private LocalDateTime readAt;
    private Long referenceId;
    private String referenceType;
    private LocalDateTime createdAt;
}
