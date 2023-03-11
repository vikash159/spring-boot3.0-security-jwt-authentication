package com.chat.payload;

import com.chat.model.ConversationType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class ConversationPayload {
    @NotNull(message = "type cannot be blank")
    private ConversationType type;
    @NotNull(message = "userIds cannot be blank")
    private List<Long> userIds;
    private String name;
}
