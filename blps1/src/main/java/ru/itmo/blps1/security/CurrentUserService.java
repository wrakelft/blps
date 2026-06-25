package ru.itmo.blps1.security;

import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.impl.identity.Authentication;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import ru.itmo.blps1.entity.User;
import ru.itmo.blps1.exception.BadRequestException;
import ru.itmo.blps1.service.user.UserServiceInt;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private final UserServiceInt userService;
    private final IdentityService identityService;

    public boolean isAuth() {
        return getCurrentUsernameOptional().isPresent();
    }

    public Optional<String> getCurrentUsernameOptional() {
        Optional<String> springUsername = getSpringSecurityUsernameOptional();

        if (springUsername.isPresent()) {
            return springUsername;
        }

        return getCamundaUsernameOptional();
    }

    public String getCurrentUsername() {
        return getCurrentUsernameOptional()
                .orElseThrow(() -> new BadRequestException("User is not authenticated"));
    }

    public User getCurrentUserEntity() {
        return userService.getUserEntityByUsername(getCurrentUsername());
    }

    public User getUserEntityByUsername(String username) {
        return userService.getUserEntityByUsername(username);
    }

    public boolean isAdmin() {
        if (isSpringSecurityAdmin()) {
            return true;
        }

        return isCurrentCamundaUserInGroup("moderators");
    }

    private Optional<String> getSpringSecurityUsernameOptional() {
        org.springframework.security.core.Authentication auth =
                SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return Optional.empty();
        }

        return Optional.ofNullable(auth.getName());
    }

    private boolean isSpringSecurityAdmin() {
        org.springframework.security.core.Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            return false;
        }

        return authentication.getAuthorities()
                .stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
    }

    private Optional<String> getCamundaUsernameOptional() {
        Authentication authentication = identityService.getCurrentAuthentication();

        if (authentication == null || authentication.getUserId() == null || authentication.getUserId().isBlank()) {
            return Optional.empty();
        }

        return Optional.of(authentication.getUserId());
    }

    private boolean isCurrentCamundaUserInGroup(String groupId) {
        Authentication authentication = identityService.getCurrentAuthentication();

        if (authentication == null) {
            return false;
        }

        List<String> groupIds = authentication.getGroupIds();

        return groupIds != null && groupIds.contains(groupId);
    }
}