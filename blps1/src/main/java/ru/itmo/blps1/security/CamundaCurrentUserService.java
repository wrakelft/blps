package ru.itmo.blps1.security;

import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.impl.identity.Authentication;
import org.springframework.stereotype.Service;
import ru.itmo.blps1.entity.User;
import ru.itmo.blps1.exception.ForbiddenException;
import ru.itmo.blps1.service.user.UserServiceInt;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CamundaCurrentUserService {

    private final IdentityService identityService;
    private final UserServiceInt userService;

    public Optional<String> getCurrentCamundaUsernameOptional() {
        Authentication authentication = identityService.getCurrentAuthentication();

        if (authentication == null || authentication.getUserId() == null || authentication.getUserId().isBlank()) {
            return Optional.empty();
        }

        return Optional.of(authentication.getUserId());
    }

    public User getCurrentCamundaUserEntity() {
        String username = getCurrentCamundaUsernameOptional()
                .orElseThrow(() -> new ForbiddenException("Camunda user is not authenticated"));

        return userService.getUserEntityByUsername(username);
    }

    public boolean isCurrentCamundaUserInGroup(String groupId) {
        Authentication authentication = identityService.getCurrentAuthentication();

        if (authentication == null) {
            return false;
        }

        List<String> groupIds = authentication.getGroupIds();

        return groupIds != null && groupIds.contains(groupId);
    }
}