package ru.itmo.blps1.bpm.delegate.user;

import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;
import ru.itmo.blps1.dto.user.CreateUserRequest;
import ru.itmo.blps1.dto.user.UserResponse;
import ru.itmo.blps1.service.user.UserServiceInt;

@Component("createUserDelegate")
@RequiredArgsConstructor
public class CreateUserDelegate implements JavaDelegate {

    private final UserServiceInt userService;

    @Override
    public void execute(DelegateExecution execution) {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername((String) execution.getVariable("username"));
        request.setEmail((String) execution.getVariable("email"));

        UserResponse response = userService.createUser(request);

        execution.setVariable("userId", response.getId());
        execution.setVariable("createdUsername", response.getUsername());
        execution.setVariable("createdEmail", response.getEmail());
    }
}