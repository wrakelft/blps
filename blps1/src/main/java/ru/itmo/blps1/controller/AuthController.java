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
import ru.itmo.blps1.service.bpm.BusinessProcessService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "Authentication operations")
public class AuthController {

    private final BusinessProcessService businessProcessService;

    @PostMapping("/login")
    @Operation(summary = "Login with JAAS XML user and get JWT")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return businessProcessService.login(request);
    }
}