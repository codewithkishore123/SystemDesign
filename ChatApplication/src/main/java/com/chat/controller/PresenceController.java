package com.chat.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chat.dto.PresenceResponse;
import com.chat.service.PresenceService;



@RestController
@RequestMapping("/api/v1/presence")
public class PresenceController {

    private final PresenceService presenceService;
    
    

    public PresenceController(PresenceService presenceService) {
		super();
		this.presenceService = presenceService;
	}



	@GetMapping("/{userId}")
    public ResponseEntity<PresenceResponse> getPresence(
            @PathVariable("userId") UUID userId,
            Authentication authentication) {

        return ResponseEntity.ok(
                presenceService.getPresence(userId)
        );
    }
}