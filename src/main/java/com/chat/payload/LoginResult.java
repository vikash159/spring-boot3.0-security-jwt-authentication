package com.chat.payload;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResult {
    private boolean success;
    private String message;
    private String token;
    private UserDto body;
}
