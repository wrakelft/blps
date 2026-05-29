package ru.itmo.blps1.security.xml;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Set;

@Getter
@AllArgsConstructor
public class XmlSecurityUser {
    private String username;
    private String password;
    private Set<String> roles;
}
