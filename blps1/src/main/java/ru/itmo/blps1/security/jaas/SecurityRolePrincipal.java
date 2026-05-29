package ru.itmo.blps1.security.jaas;

import java.security.Principal;

public class SecurityRolePrincipal implements Principal {

    private final String name;

    public SecurityRolePrincipal(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
}
