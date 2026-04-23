package ru.itmo.blps1.service.user;

import ru.itmo.blps1.dto.user.CreateUserRequest;
import ru.itmo.blps1.dto.user.UserResponse;
import ru.itmo.blps1.entity.User;

import java.util.List;

public interface UserServiceInt {

    UserResponse createUser(CreateUserRequest request);

    List<UserResponse> getAllUsers();

    UserResponse getUserById(Long id);

    User getUserEntityById(Long id);
}
