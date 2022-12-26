package com.chat.payload;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginPayload {

    @NotBlank
    private String username;

    @NotBlank
    private String password;
}
