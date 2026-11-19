package com.chat.websocket;

import java.security.Principal;
import java.util.UUID;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import com.chat.dto.ChatReceiptRequest;
import com.chat.dto.ReceiptResponse;
import com.chat.enums.MessageStatus;
import com.chat.service.MessageReceiptService;


@Controller
public class ChatReceiptWebSocketController {

    private final MessageReceiptService receiptService;
    
    

    public ChatReceiptWebSocketController(MessageReceiptService receiptService) {
		super();
		this.receiptService = receiptService;
	}

	@MessageMapping("/chat.delivered")
    public ReceiptResponse markDelivered(
            @Payload ChatReceiptRequest request,
            Principal principal) {

        UUID recipientId = UUID.fromString(principal.getName());

        return receiptService.updateReceipt(
                recipientId,
                request,
                MessageStatus.DELIVERED
        );
    }

    @MessageMapping("/chat.read")
    public ReceiptResponse markRead(
            @Payload ChatReceiptRequest request,
            Principal principal) {

        UUID recipientId = UUID.fromString(principal.getName());

        return receiptService.updateReceipt(
                recipientId,
                request,
                MessageStatus.READ
        );
    }
}
