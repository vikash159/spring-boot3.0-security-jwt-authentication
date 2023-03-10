package com.chat.payload;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class UserResult {
    private boolean success;
    private String message;
    private List<UserDto> body;
    private Page page;
}
