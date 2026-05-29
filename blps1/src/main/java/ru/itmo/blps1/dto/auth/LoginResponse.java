package ru.itmo.blps1.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Set;

@Getter
@AllArgsConstructor
public class LoginResponse {

    private String token;
    private String username;
    private Set<String> roles;
}
