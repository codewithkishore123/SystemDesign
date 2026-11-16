package com.notification.dto;

import java.util.Map;

import com.notification.enums.NotificationChannel;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record NotificationRequest(
		 @NotBlank
	        String sourceService,

	        @NotBlank
	        String eventType,

	        @NotBlank
	        String recipientId,

	        @NotNull
	        NotificationChannel channel,

	        @NotBlank
	        String destination,

	        String templateCode,

	        Map<String, Object> payload,

	        @NotBlank
	        String idempotencyKey) {

}
