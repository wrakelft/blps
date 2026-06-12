package ru.itmo.blps1.integration.bitrix24.jca;

import jakarta.resource.ResourceException;
import jakarta.resource.spi.ConnectionManager;
import jakarta.resource.spi.ConnectionRequestInfo;
import jakarta.resource.spi.ManagedConnection;
import jakarta.resource.spi.ManagedConnectionFactory;
import org.springframework.web.client.RestClient;

import javax.security.auth.Subject;
import java.io.PrintWriter;
import java.util.Set;

public class Bitrix24ManagedConnectionFactory implements ManagedConnectionFactory {

    private final RestClient.Builder restClientBuilder;

    private String webhookUrl;

    private Long responsibleId;

    private PrintWriter logWriter;

    public Bitrix24ManagedConnectionFactory(
            RestClient.Builder restClientBuilder,
            String webhookUrl,
            Long responsibleId
    ) {
        this.restClientBuilder = restClientBuilder;
        this.webhookUrl = webhookUrl;
        this.responsibleId = responsibleId;
    }

    @Override
    public Object createConnectionFactory(ConnectionManager cxManager) throws ResourceException {
        return new Bitrix24ConnectionFactory(this, cxManager);
    }

    @Override
    public Object createConnectionFactory() throws ResourceException {
        return new Bitrix24ConnectionFactory(this, new Bitrix24SimpleConnectionManager());
    }

    @Override
    public ManagedConnection createManagedConnection(
            Subject subject,
            ConnectionRequestInfo connectionRequestInfo
    ) throws ResourceException {
        Bitrix24ConnectionRequestInfo requestInfo = resolveRequestInfo(connectionRequestInfo);

        return new Bitrix24ManagedConnection(
                restClientBuilder.build(),
                requestInfo.webhookUrl(),
                requestInfo.responsibleId()
        );
    }

    @Override
    public ManagedConnection matchManagedConnections(
            Set connectionSet,
            Subject subject,
            ConnectionRequestInfo connectionRequestInfo
    ) throws ResourceException {
        if (connectionSet == null || connectionSet.isEmpty()) {
            return null;
        }

        for (Object candidate : connectionSet) {
            if (candidate instanceof Bitrix24ManagedConnection managedConnection) {
                return managedConnection;
            }
        }

        return null;
    }

    @Override
    public void setLogWriter(PrintWriter out) throws ResourceException {
        this.logWriter = out;
    }

    @Override
    public PrintWriter getLogWriter() throws ResourceException {
        return logWriter;
    }

    private Bitrix24ConnectionRequestInfo resolveRequestInfo(ConnectionRequestInfo connectionRequestInfo) throws ResourceException {
        if (connectionRequestInfo == null) {
            return new Bitrix24ConnectionRequestInfo(webhookUrl, responsibleId);
        }

        if (connectionRequestInfo instanceof Bitrix24ConnectionRequestInfo bitrixInfo) {
            return bitrixInfo;
        }

        throw new ResourceException("Unsupported connection request info: " + connectionRequestInfo.getClass());
    }

    public String getWebhookUrl() {
        return webhookUrl;
    }

    public void setWebhookUrl(String webhookUrl) {
        this.webhookUrl = webhookUrl;
    }

    public Long getResponsibleId() {
        return responsibleId;
    }

    public void setResponsibleId(Long responsibleId) {
        this.responsibleId = responsibleId;
    }
}
