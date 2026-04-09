package ru.itmo.blps1.dto.user;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UserResponse {

    private Long id;
    private String username;
    private String email;
}
