package ru.itmo.blps1.security.jaas;

import java.security.Principal;

public class SecurityUserPrincipal implements Principal {

    private final String name;

    public SecurityUserPrincipal(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
}
