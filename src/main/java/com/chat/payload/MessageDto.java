package com.chat.payload;

import com.chat.model.MessageStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageDto {
    private Long id;
    private String uuid;
    private String type;
    private String content;
    private MessageStatus status;
    private UserDto user;
    private ConversationDto conversation;
    private Instant createdAt;
}