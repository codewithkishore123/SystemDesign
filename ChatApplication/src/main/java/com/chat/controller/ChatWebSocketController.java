package com.chat.controller;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import com.chat.dto.ChatMessageEvent;
import com.chat.dto.ChatMessageRequest;
import com.chat.dto.MessageRequest;
import com.chat.dto.MessageResponse;
import com.chat.messaging.MessageEventPublisher;
import com.chat.service.MessageService;

import jakarta.validation.Valid;


@Controller
public class ChatWebSocketController {

    private final MessageService messageService;

    private final MessageEventPublisher messageEventPublisher;
    
    

    public ChatWebSocketController(MessageService messageService, MessageEventPublisher messageEventPublisher) {
		super();
		this.messageService = messageService;
		this.messageEventPublisher = messageEventPublisher;
	}


   

    @MessageMapping("/chat.send")
    public void sendMessage(
            @Valid @Payload ChatMessageRequest request,
            Principal principal) {

        UUID senderId = UUID.fromString(principal.getName());

        UUID conversationId = request.conversationId();

        // 1. Persist message in PostgreSQL
        MessageResponse response = messageService.sendMessage(
                conversationId,
                senderId,
                new MessageRequest(request.content())
        );

        // 2. Retrieve conversation participants
        List<UUID> participantIds =
                messageService.getConversationParticipantIds(conversationId);

        // 3. Publish event to RabbitMQ
        ChatMessageEvent event =
                new ChatMessageEvent(response, participantIds);

        messageEventPublisher.publish(event);
    }
}
