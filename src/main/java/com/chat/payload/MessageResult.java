package com.chat.payload;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class MessageResult {
    private boolean success;
    private List<MessageDto> body;
}
