package com.chat.payload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;


@Data
public class SignUpPayload {

    @NotBlank
    @Size(min = 3, max = 40, message = "minimum name length is 3")
    private String name;

    @NotBlank
    @Size(min = 3, max = 20, message = "minimum username length is 3")
    private String username;

    @NotBlank(message = "password cannot be blank")
    @Size(min = 6, max = 20, message = "minimum password length is 6")
    private String password;
}
