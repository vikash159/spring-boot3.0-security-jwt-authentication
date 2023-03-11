package com.chat.payload;

import com.chat.model.ConversationType;
import com.chat.model.Group;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.Date;

@Data
@Builder
public class ConversationDto {
    private Long id;
    private String type;
    private String message;
    private Date messageAt;
    private UserDto user;
    private Group group;
    private Instant createdAt;
}
