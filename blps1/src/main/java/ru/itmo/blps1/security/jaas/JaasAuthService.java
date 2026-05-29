package ru.itmo.blps1.security.jaas;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.itmo.blps1.exception.BadRequestException;
import ru.itmo.blps1.security.xml.XmlSecurityUser;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.LoginContext;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JaasAuthService {

    private static final String JAAS_CONFIG_NAME = "XmlJaasLogin";

    @Value("${security.xml-users-path}")
    private String xmlUsersPath;

    public XmlSecurityUser authenticate(String username, String password) {
        try {
            Subject subject = new Subject();

            LoginContext loginContext = new LoginContext(
                    JAAS_CONFIG_NAME,
                    subject,
                    createCallbackHandler(username, password),
                    createJaasConfiguration()
            );

            loginContext.login();

            Set<String> roles = subject.getPrincipals(SecurityRolePrincipal.class)
                    .stream()
                    .map(Principal::getName)
                    .collect(Collectors.toSet());

            return new XmlSecurityUser(username, password, roles);
        } catch (Exception e) {
            throw new BadRequestException("Invalid username or password");
        }
    }

    private CallbackHandler createCallbackHandler(String username, String password) {
        return callbacks -> {
            for (Callback callback : callbacks) {
                if (callback instanceof NameCallback nameCallback) {
                    nameCallback.setName(username);
                } else if (callback instanceof PasswordCallback passwordCallback) {
                    passwordCallback.setPassword(password.toCharArray());
                }
            }
        };
    }

    private Configuration createJaasConfiguration() {
        return new Configuration() {
            @Override
            public AppConfigurationEntry[] getAppConfigurationEntry(String name) {
                if (!JAAS_CONFIG_NAME.equals(name)) {
                    return null;
                }

                Map<String, Object> options = new HashMap<>();
                options.put("usersFile", xmlUsersPath);

                return new AppConfigurationEntry[]{
                        new AppConfigurationEntry(
                                XmlLoginModule.class.getName(),
                                AppConfigurationEntry.LoginModuleControlFlag.REQUIRED,
                                options
                        )
                };
            }
        };
    }
}