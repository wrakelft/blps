package ru.itmo.blps1.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.itmo.blps1.dto.auth.LoginRequest;
import ru.itmo.blps1.dto.auth.LoginResponse;
import ru.itmo.blps1.security.jaas.JaasAuthService;
import ru.itmo.blps1.security.jwt.JwtService;
import ru.itmo.blps1.security.xml.XmlSecurityUser;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "Authentication operations")
public class AuthController {

    private final JaasAuthService jaasAuthService;

    private final JwtService jwtService;

    @PostMapping("/login")
    @Operation(summary = "Login with JAAS XML user and get JWT")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        XmlSecurityUser user = jaasAuthService.authenticate(
                request.getUsername(),
                request.getPassword()
        );

        String token = jwtService.generateToken(user.getUsername(), user.getRoles());

        return new LoginResponse(token, user.getUsername(), user.getRoles());
    }
}