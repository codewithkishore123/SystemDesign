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

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.notification.dto.NotificationDetailsResponse;
import com.notification.dto.NotificationStatusResponse;


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
	    
	    @GetMapping("/{id}")
	    public ResponseEntity<NotificationDetailsResponse> getNotification(
	            @PathVariable UUID id) {

	        return ResponseEntity.ok(
	                notificationService.getNotificationDetails(id)
	        );
	    }


	    @GetMapping("/{id}/status")
	    public ResponseEntity<NotificationStatusResponse> getNotificationStatus(
	            @PathVariable UUID id) {

	        return ResponseEntity.ok(
	                notificationService.getNotificationStatus(id)
	        );
	    }


	    @GetMapping
	    public ResponseEntity<Page<NotificationDetailsResponse>> getAllNotifications(
	            @RequestParam(defaultValue = "0") int page,
	            @RequestParam(defaultValue = "10") int size) {

	        if (page < 0 || size < 1 || size > 100) {
	            throw new IllegalArgumentException(
	                    "Page must be >= 0 and size must be between 1 and 100"
	            );
	        }

	        Pageable pageable = PageRequest.of(
	                page,
	                size,
	                Sort.by(Sort.Direction.DESC, "createdAt")
	        );

	        return ResponseEntity.ok(
	                notificationService.getAllNotifications(pageable)
	        );
	    }

}
