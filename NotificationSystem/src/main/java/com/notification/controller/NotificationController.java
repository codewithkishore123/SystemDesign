package com.notification.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.notification.dto.NotificationRequest;
import com.notification.dto.NotificationResponse;
import com.notification.service.NotificationService;

import jakarta.validation.Valid;


@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {
	
	 private final NotificationService notificationService;

	    public NotificationController(
	            NotificationService notificationService) {
	        this.notificationService = notificationService;
	    }

	    @PostMapping
	    public ResponseEntity<NotificationResponse> createNotification(
	            @Valid @RequestBody NotificationRequest request) {

	        NotificationResponse response =
	                notificationService.createNotification(request);

	        return ResponseEntity
	                .status(HttpStatus.ACCEPTED)
	                .body(response);
	    }

}
