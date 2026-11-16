package com.notification.dto;

import java.util.UUID;

import com.notification.enums.NotificationStatus;

public record NotificationResponse( 
		UUID notificationId,
        NotificationStatus status,
        String message) {

}
