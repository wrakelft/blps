package ru.itmo.blps1.mapper;

import org.springframework.stereotype.Component;
import ru.itmo.blps1.dto.user.CreateUserRequest;
import ru.itmo.blps1.dto.user.UserResponse;
import ru.itmo.blps1.entity.User;

@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        if (user == null) {
            return null;
        }

        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .build();
    }

    public User toEntity(CreateUserRequest request) {
        if (request == null) {
            return null;
        }

        return User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .build();
    }
}