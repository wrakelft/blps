package ru.itmo.blps1.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import ru.itmo.blps1.entity.User;
import ru.itmo.blps1.exception.BadRequestException;
import ru.itmo.blps1.repository.UserRepository;
import ru.itmo.blps1.service.user.UserServiceInt;

@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private final UserServiceInt userService;

    public String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new BadRequestException("User isn't authenticated");
        }

        return auth.getName();
    }

    public User getCurrentUserEntity() {
        return userService.getUserEntityByUsername(getCurrentUsername());
    }

    public boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null) {
            return false;
        }

        return auth.getAuthorities()
                .stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
    }
}
