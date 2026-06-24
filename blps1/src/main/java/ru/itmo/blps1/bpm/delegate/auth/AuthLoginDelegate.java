package ru.itmo.blps1.bpm.delegate.auth;

import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;
import ru.itmo.blps1.security.jaas.JaasAuthService;
import ru.itmo.blps1.security.jwt.JwtService;
import ru.itmo.blps1.security.xml.XmlSecurityUser;

@Component("authLoginDelegate")
@RequiredArgsConstructor
public class AuthLoginDelegate implements JavaDelegate {

    private final JaasAuthService jaasAuthService;
    private final JwtService jwtService;

    @Override
    public void execute(DelegateExecution execution) {
        String username = (String) execution.getVariable("username");
        String password = (String) execution.getVariable("password");

        XmlSecurityUser user = jaasAuthService.authenticate(username, password);

        String token = jwtService.generateToken(user.getUsername(), user.getRoles());

        execution.setVariable("authToken", token);
        execution.setVariable("authUsername", user.getUsername());
        execution.setVariable("authRolesCsv", String.join(",", user.getRoles()));
    }
}