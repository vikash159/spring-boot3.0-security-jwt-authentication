package com.chat.api;

import com.chat.payload.MessageDto;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class ChatController {
    private final SimpMessagingTemplate simpMessagingTemplate;

    public ChatController(SimpMessagingTemplate simpMessagingTemplate) {
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    @MessageMapping("/chat.message/{userId}")
    public void sendMessage(@Payload MessageDto messageDto,
                            @DestinationVariable("userId") Long userId) {
        simpMessagingTemplate.convertAndSend("/chat/" + userId, messageDto);
    }
}