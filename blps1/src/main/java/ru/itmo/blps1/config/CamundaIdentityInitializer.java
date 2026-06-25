package ru.itmo.blps1.config;

import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.AuthorizationService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.authorization.Authorization;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.User;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import ru.itmo.blps1.repository.UserRepository;
import ru.itmo.blps1.security.xml.XmlSecurityUser;
import ru.itmo.blps1.security.xml.XmlSecurityUserLoader;

@Component
@RequiredArgsConstructor
public class CamundaIdentityInitializer implements CommandLineRunner {

    private static final String CAMUNDA_GROUP_USERS = "users";
    private static final String CAMUNDA_GROUP_MODERATORS = "moderators";

    private final IdentityService identityService;
    private final XmlSecurityUserLoader xmlSecurityUserLoader;
    private final AuthorizationService authorizationService;
    private final UserRepository userRepository;

    @Override
    public void run(String... args) {
        createGroupIfMissing(CAMUNDA_GROUP_USERS, "Application users", "WORKFLOW");
        createGroupIfMissing(CAMUNDA_GROUP_MODERATORS, "Board moderators", "WORKFLOW");

        deleteBroadTaskAuthorizationForUsers();

        createTasklistAuthorizationForGroup(CAMUNDA_GROUP_USERS);
        createTasklistAuthorizationForGroup(CAMUNDA_GROUP_MODERATORS);
        createFilterAuthorizationForGroup(CAMUNDA_GROUP_USERS);
        createFilterAuthorizationForGroup(CAMUNDA_GROUP_MODERATORS);
        createProcessStartAuthorizationForGroup(CAMUNDA_GROUP_USERS);
        createProcessStartAuthorizationForGroup(CAMUNDA_GROUP_MODERATORS);

        createTaskAuthorizationForModerators();


        for (XmlSecurityUser xmlUser : xmlSecurityUserLoader.loadUsers()) {
            createCamundaUserIfMissing(xmlUser);
            syncCamundaMemberships(xmlUser);
            createAppUserIfMissing(xmlUser);
        }
    }

    private void createGroupIfMissing(String id, String name, String type) {
        if (identityService.createGroupQuery().groupId(id).singleResult() != null) {
            return;
        }

        Group group = identityService.newGroup(id);
        group.setName(name);
        group.setType(type);
        identityService.saveGroup(group);
    }

    private void createCamundaUserIfMissing(XmlSecurityUser xmlUser) {
        User existingUser = identityService.createUserQuery()
                .userId(xmlUser.getUsername())
                .singleResult();

        if (existingUser != null) {
            return;
        }

        User camundaUser = identityService.newUser(xmlUser.getUsername());
        camundaUser.setPassword(xmlUser.getPassword());
        camundaUser.setFirstName(xmlUser.getUsername());
        camundaUser.setLastName("BLPS");
        camundaUser.setEmail(xmlUser.getUsername() + "@blps.local");

        identityService.saveUser(camundaUser);
    }

    private void syncCamundaMemberships(XmlSecurityUser xmlUser) {
        if (xmlUser.getRoles().contains("USER")) {
            addUserToGroupIfMissing(xmlUser.getUsername(), CAMUNDA_GROUP_USERS);
        }

        if (xmlUser.getRoles().contains("ADMIN")) {
            addUserToGroupIfMissing(xmlUser.getUsername(), CAMUNDA_GROUP_MODERATORS);
        }
    }

    private void addUserToGroupIfMissing(String userId, String groupId) {
        boolean alreadyMember = identityService.createUserQuery()
                .userId(userId)
                .memberOfGroup(groupId)
                .count() > 0;

        if (!alreadyMember) {
            identityService.createMembership(userId, groupId);
        }
    }

    private void createAppUserIfMissing(XmlSecurityUser xmlUser) {
        if (userRepository.existsByUsername(xmlUser.getUsername())) {
            return;
        }

        ru.itmo.blps1.entity.User appUser = ru.itmo.blps1.entity.User.builder()
                .username(xmlUser.getUsername())
                .email(xmlUser.getUsername() + "@blps.local")
                .build();

        userRepository.save(appUser);
    }

    private void createTasklistAuthorizationForGroup(String groupId) {
        createGrantIfMissing(
                groupId,
                Resources.APPLICATION,
                "tasklist",
                Permissions.ACCESS
        );
    }

    private void createFilterAuthorizationForGroup(String groupId) {
        createGrantIfMissing(
                groupId,
                Resources.FILTER,
                "*",
                Permissions.READ
        );
    }

    private void createTaskAuthorizationForModerators() {
        createGrantIfMissing(
                CAMUNDA_GROUP_MODERATORS,
                Resources.TASK,
                "*",
                Permissions.READ,
                Permissions.UPDATE,
                Permissions.TASK_ASSIGN,
                Permissions.TASK_WORK
        );
    }

    private void createGrantIfMissing(
            String groupId,
            Resources resource,
            String resourceId,
            Permissions... permissions
    ) {
        Authorization existingAuthorization = authorizationService
                .createAuthorizationQuery()
                .groupIdIn(groupId)
                .resourceType(resource)
                .resourceId(resourceId)
                .singleResult();

        if (existingAuthorization == null) {
            Authorization authorization = authorizationService.createNewAuthorization(Authorization.AUTH_TYPE_GRANT);
            authorization.setGroupId(groupId);
            authorization.setResource(resource);
            authorization.setResourceId(resourceId);

            for (Permissions permission : permissions) {
                authorization.addPermission(permission);
            }

            authorizationService.saveAuthorization(authorization);
            return;
        }

        boolean changed = false;

        for (Permissions permission : permissions) {
            if (!existingAuthorization.isPermissionGranted(permission)) {
                existingAuthorization.addPermission(permission);
                changed = true;
            }
        }

        if (changed) {
            authorizationService.saveAuthorization(existingAuthorization);
        }
    }

    private void createProcessStartAuthorizationForGroup(String groupId) {
        createGrantIfMissing(
                groupId,
                Resources.PROCESS_DEFINITION,
                "*",
                Permissions.READ,
                Permissions.CREATE_INSTANCE
        );

        createGrantIfMissing(
                groupId,
                Resources.PROCESS_INSTANCE,
                "*",
                Permissions.CREATE
        );
    }

    private void deleteBroadTaskAuthorizationForUsers() {
        authorizationService
                .createAuthorizationQuery()
                .groupIdIn(CAMUNDA_GROUP_USERS)
                .resourceType(Resources.TASK)
                .resourceId("*")
                .list()
                .forEach(authorization -> authorizationService.deleteAuthorization(authorization.getId()));
    }
}