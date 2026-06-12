package ru.itmo.blps1.integration.bitrix24.jca;

import jakarta.resource.ResourceException;
import jakarta.resource.spi.ConnectionManager;

public class Bitrix24ConnectionFactory {

    private final Bitrix24ManagedConnectionFactory managedConnectionFactory;

    private final ConnectionManager connectionManager;

    public Bitrix24ConnectionFactory(
            Bitrix24ManagedConnectionFactory managedConnectionFactory,
            ConnectionManager connectionManager
    ) {
        this.managedConnectionFactory = managedConnectionFactory;
        this.connectionManager = connectionManager;
    }

    public Bitrix24Connection getConnection() {
        try {
            Bitrix24ConnectionRequestInfo requestInfo = new Bitrix24ConnectionRequestInfo(
                    managedConnectionFactory.getWebhookUrl(),
                    managedConnectionFactory.getResponsibleId()
            );

            return (Bitrix24Connection) connectionManager.allocateConnection(
                    managedConnectionFactory,
                    requestInfo
            );
        } catch (ResourceException exception) {
            throw new IllegalStateException("Failed to allocate Bitrix24 JCA connection", exception);
        }
    }
}