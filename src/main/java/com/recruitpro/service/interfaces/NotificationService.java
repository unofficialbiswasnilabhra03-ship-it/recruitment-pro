package com.recruitpro.service.interfaces;

import com.recruitpro.dto.response.NotificationResponse;
import com.recruitpro.enums.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationService {

    // User-facing API
    Page<NotificationResponse> getMyNotifications(Long userId, Pageable pageable);

    Page<NotificationResponse> getMyUnreadNotifications(Long userId, Pageable pageable);

    long getUnreadCount(Long userId);

    void markAsRead(Long notificationId, Long userId);

    void markAllAsRead(Long userId);

    // Internal — called by other services when business events occur
    void createNotification(Long userId, NotificationType type,
                            String title, String message,
                            Long referenceId, String referenceType);
}
