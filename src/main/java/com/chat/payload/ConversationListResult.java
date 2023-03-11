package com.chat.payload;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ConversationListResult {
    private boolean success;
    private String message;
    private List<ConversationDto> body;
}
