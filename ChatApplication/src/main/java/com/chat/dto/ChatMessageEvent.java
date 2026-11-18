package com.chat.dto;

import java.util.List;
import java.util.UUID;

public record ChatMessageEvent(

        MessageResponse message,

        List<UUID> participantIds

) {
}
