package com.chat.websocket;

import java.security.Principal;
import java.util.UUID;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import com.chat.dto.HeartbeatRequest;
import com.chat.service.PresenceService;



@Controller
public class PresenceWebSocketController {

    private final PresenceService presenceService;
    
    

    public PresenceWebSocketController(PresenceService presenceService) {
		super();
		this.presenceService = presenceService;
	}



	@MessageMapping("/presence.heartbeat")
    public void heartbeat(
            HeartbeatRequest request,
            Principal principal) {

        if (!"HEARTBEAT".equals(request.type())) {
            return;
        }

        UUID userId = UUID.fromString(principal.getName());

        presenceService.heartbeat(userId);
    }
}