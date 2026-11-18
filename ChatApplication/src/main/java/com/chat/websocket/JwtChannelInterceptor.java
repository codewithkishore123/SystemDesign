package com.chat.websocket;

import java.util.List;
import java.util.UUID;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import com.chat.security.JwtService;



@Component
public class JwtChannelInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;
    

    public JwtChannelInterceptor(JwtService jwtService) {
		super();
		this.jwtService = jwtService;
	}


	@Override
    public Message<?> preSend(
            Message<?> message,
            MessageChannel channel) {

        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(
                        message, StompHeaderAccessor.class);

        if (accessor == null) {
            return message;
        }

        StompCommand command = accessor.getCommand();

        if (StompCommand.CONNECT.equals(command)) {

            String authorization =
                    accessor.getFirstNativeHeader("Authorization");

            if (authorization == null ||
                    !authorization.startsWith("Bearer ")) {
                throw new AccessDeniedException("Missing JWT token");
            }

            String token = authorization.substring(7);

            try {

                if (!jwtService.isTokenValid(token)) {
                    throw new AccessDeniedException("Invalid JWT token");
                }

                UUID userId = jwtService.extractUserId(token);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userId.toString(),
                                null,
                                List.of(new SimpleGrantedAuthority("ROLE_USER"))
                        );

                accessor.setUser(authentication);

            } catch (AccessDeniedException exception) {
                throw exception;
            } catch (Exception exception) {
                throw new AccessDeniedException("Invalid JWT token");
            }
        }

        if ((StompCommand.SEND.equals(command)
                || StompCommand.SUBSCRIBE.equals(command))
                && accessor.getUser() == null) {

            throw new AccessDeniedException(
                    "WebSocket authentication required");
        }

        return message;
    }
}
