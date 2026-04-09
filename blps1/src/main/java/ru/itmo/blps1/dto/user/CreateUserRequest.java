package ru.itmo.blps1.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateUserRequest {

    @NotBlank(message = "Username must not be blank")
    @Size(max = 100, message = "Username must be at most 100 characters")
    private String username;

    @NotBlank(message = "Email must not be blank")
    @Email(message = "Email must be valid")
    @Size(max = 150, message = "Email must be at most 150 characters")
    private String email;
}