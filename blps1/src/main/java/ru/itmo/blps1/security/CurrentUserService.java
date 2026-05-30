package ru.itmo.blps1.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import ru.itmo.blps1.entity.User;
import ru.itmo.blps1.exception.BadRequestException;
import ru.itmo.blps1.repository.UserRepository;
import ru.itmo.blps1.service.user.UserServiceInt;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private final UserServiceInt userService;

    public boolean isAuth() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        return auth != null
                && auth.isAuthenticated()
                && !(auth instanceof AnonymousAuthenticationToken);
    }

    public Optional<String> getCurrentUsernameOptional() {
        if(!isAuth()) {
            return Optional.empty();
        }

        return Optional.of(SecurityContextHolder.getContext().getAuthentication().getName());
    }

    public String getCurrentUsername() {
        return getCurrentUsernameOptional()
                .orElseThrow(() -> new BadRequestException("User is not authenticated"));
    }

    public User getCurrentUserEntity() {
        return userService.getUserEntityByUsername(getCurrentUsername());
    }

    public boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            return false;
        }

        return authentication.getAuthorities()
                .stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
    }
}
