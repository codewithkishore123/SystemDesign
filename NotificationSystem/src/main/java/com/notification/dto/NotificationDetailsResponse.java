package com.notification.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.notification.enums.NotificationChannel;
import com.notification.enums.NotificationStatus;

public record NotificationDetailsResponse(
        UUID notificationId,
        NotificationStatus status,
        NotificationChannel channel,
        String destination,
        String eventType,
        String sourceService,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
