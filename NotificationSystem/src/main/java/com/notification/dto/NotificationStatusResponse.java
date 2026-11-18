package com.notification.dto;

import java.util.UUID;

import com.notification.enums.NotificationStatus;

public record NotificationStatusResponse(
        UUID notificationId,
        NotificationStatus status
) {
}
