package ru.itmo.blps1.security.jaas;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import ru.itmo.blps1.security.xml.XmlSecurityUser;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class XmlLoginModule implements LoginModule {

    private Subject subject;
    private CallbackHandler callbackHandler;
    private Map<String, ?> options;

    private String username;
    private XmlSecurityUser authenticatedUser;

    private SecurityUserPrincipal userPrincipal;
    private final Set<SecurityRolePrincipal> rolePrincipals = new HashSet<>();

    @Override
    public void initialize(
            Subject subject,
            CallbackHandler callbackHandler,
            Map<String, ?> sharedState,
            Map<String, ?> options
    ) {
        this.subject = subject;
        this.callbackHandler = callbackHandler;
        this.options = options;
    }

    @Override
    public boolean login() throws LoginException {
        if (callbackHandler == null) {
            throw new LoginException("CallbackHandler is not configured");
        }

        NameCallback nameCallback = new NameCallback("username");
        PasswordCallback passwordCallback = new PasswordCallback("password", false);

        try {
            callbackHandler.handle(new Callback[]{nameCallback, passwordCallback});

            username = nameCallback.getName();
            String password = new String(passwordCallback.getPassword());

            Object usersFileOption = options.get("usersFile");
            String usersFile = usersFileOption != null
                    ? usersFileOption.toString()
                    : "security-users.xml";

            XmlSecurityUser user = findUserInXml(usersFile, username)
                    .orElseThrow(() -> new LoginException("Invalid username or password"));

            if (!user.getPassword().equals(password)) {
                throw new LoginException("Invalid username or password");
            }

            authenticatedUser = user;
            return true;
        } catch (LoginException e) {
            throw e;
        } catch (Exception e) {
            LoginException loginException = new LoginException("Failed to authenticate user by XML");
            loginException.initCause(e);
            throw loginException;
        }
    }

    @Override
    public boolean commit() throws LoginException {
        if (authenticatedUser == null) {
            return false;
        }

        userPrincipal = new SecurityUserPrincipal(authenticatedUser.getUsername());
        subject.getPrincipals().add(userPrincipal);

        for (String role : authenticatedUser.getRoles()) {
            SecurityRolePrincipal rolePrincipal = new SecurityRolePrincipal(role);
            rolePrincipals.add(rolePrincipal);
            subject.getPrincipals().add(rolePrincipal);
        }

        return true;
    }

    @Override
    public boolean abort() {
        clearState();
        return true;
    }

    @Override
    public boolean logout() {
        if (userPrincipal != null) {
            subject.getPrincipals().remove(userPrincipal);
        }

        subject.getPrincipals().removeAll(rolePrincipals);
        clearState();

        return true;
    }

    private void clearState() {
        username = null;
        authenticatedUser = null;
        userPrincipal = null;
        rolePrincipals.clear();
    }

    private Optional<XmlSecurityUser> findUserInXml(String usersFile, String username) throws Exception {
        try (InputStream inputStream = openUsersFile(usersFile)) {
            if (inputStream == null) {
                throw new IllegalStateException("XML users file not found: " + usersFile);
            }

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setExpandEntityReferences(false);

            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(inputStream);
            document.getDocumentElement().normalize();

            NodeList userNodes = document.getElementsByTagName("user");

            for (int i = 0; i < userNodes.getLength(); i++) {
                Element userElement = (Element) userNodes.item(i);

                String xmlUsername = getText(userElement, "username");

                if (!xmlUsername.equals(username)) {
                    continue;
                }

                String xmlPassword = getText(userElement, "password");

                Set<String> roles = new HashSet<>();
                NodeList roleNodes = userElement.getElementsByTagName("role");

                for (int j = 0; j < roleNodes.getLength(); j++) {
                    roles.add(roleNodes.item(j).getTextContent().trim());
                }

                return Optional.of(new XmlSecurityUser(xmlUsername, xmlPassword, roles));
            }

            return Optional.empty();
        }
    }

    private InputStream openUsersFile(String usersFile) {
        return Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream(usersFile);
    }

    private String getText(Element element, String tagName) {
        NodeList nodes = element.getElementsByTagName(tagName);

        if (nodes.getLength() == 0) {
            throw new IllegalStateException("Missing XML tag: " + tagName);
        }

        return nodes.item(0).getTextContent().trim();
    }
}