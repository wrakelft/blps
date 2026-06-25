package ru.itmo.blps1.security.xml;

import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import ru.itmo.blps1.exception.BadRequestException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.ArrayList;

@Component
public class XmlSecurityUserLoader {

    private static final String DEFAULT_USERS_FILE = "security-users.xml";

    public List<XmlSecurityUser> loadUsers() {
        return loadUsers(DEFAULT_USERS_FILE);
    }

    public Optional<XmlSecurityUser> findUserByUsername(String username) {
        return loadUsers().stream()
                .filter(user -> user.getUsername().equals(username))
                .findFirst();
    }

    public List<XmlSecurityUser> loadUsers(String usersFile) {
        try (InputStream inputStream = openUsersFile(usersFile)) {
            if (inputStream == null) {
                throw new BadRequestException("XML users file not found: " + usersFile);
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
            List<XmlSecurityUser> users = new ArrayList<>();

            for (int i = 0; i < userNodes.getLength(); i++) {
                Element userElement = (Element) userNodes.item(i);

                String username = getText(userElement, "username");
                String password = getText(userElement, "password");

                Set<String> roles = new HashSet<>();
                NodeList roleNodes = userElement.getElementsByTagName("role");

                for (int j = 0; j < roleNodes.getLength(); j++) {
                    roles.add(roleNodes.item(j).getTextContent().trim());
                }

                users.add(new XmlSecurityUser(username, password, roles));
            }

            return users;
        } catch (Exception e) {
            throw new BadRequestException("Failed to read XML security users: " + e.getMessage());
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