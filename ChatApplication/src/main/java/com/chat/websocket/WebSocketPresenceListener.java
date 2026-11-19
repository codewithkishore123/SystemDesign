package com.chat.websocket;

import java.security.Principal;
import java.util.UUID;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import com.chat.service.PresenceService;


@Component
public class WebSocketPresenceListener {

    private final PresenceService presenceService;

    
    
    public WebSocketPresenceListener(PresenceService presenceService) {
		super();
		this.presenceService = presenceService;
	}

	@EventListener
    public void handleConnect(SessionConnectedEvent event) {

        StompHeaderAccessor accessor =
                StompHeaderAccessor.wrap(event.getMessage());

        Principal principal = accessor.getUser();

        if (principal != null) {
            UUID userId = UUID.fromString(principal.getName());
            presenceService.markOnline(userId);
        }
    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {

        Principal principal = event.getUser();

        if (principal != null) {
            UUID userId = UUID.fromString(principal.getName());
            presenceService.markOffline(userId);
        }
    }
}