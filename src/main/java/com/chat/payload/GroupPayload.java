package com.chat.payload;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class GroupPayload {
    @NotNull(message = "name cannot be blank")
    private String name;
    private List<Long> userIds;
}
