package ru.itmo.blps1.integration.bitrix24.jca;

import jakarta.resource.ResourceException;
import jakarta.resource.spi.ConnectionManager;
import jakarta.resource.spi.ConnectionRequestInfo;
import jakarta.resource.spi.ManagedConnectionFactory;

import javax.security.auth.Subject;
import java.io.Serial;

public class Bitrix24SimpleConnectionManager implements ConnectionManager {

    @Serial
    private static final long serialVersionUID = 1L;

    @Override
    public Object allocateConnection(
            ManagedConnectionFactory managedConnectionFactory,
            ConnectionRequestInfo connectionRequestInfo
    ) throws ResourceException {
        Subject subject = null;

        return managedConnectionFactory
                .createManagedConnection(subject, connectionRequestInfo)
                .getConnection(subject, connectionRequestInfo);
    }
}