package ru.itmo.blps1.integration.bitrix24.jca;

import jakarta.resource.ResourceException;
import jakarta.resource.spi.*;
import org.springframework.web.client.RestClient;

import javax.security.auth.Subject;
import javax.transaction.xa.XAResource;
import java.io.PrintWriter;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class Bitrix24ManagedConnection implements ManagedConnection {

    private final RestClient restClient;

    private final String webhookUrl;

    private final Long responsibleId;

    private final Set<ConnectionEventListener> listeners = new CopyOnWriteArraySet<>();

    private PrintWriter logWriter;

    public Bitrix24ManagedConnection(
            RestClient restClient,
            String webhookUrl,
            Long responsibleId
    ) {
        this.restClient = restClient;
        this.webhookUrl = webhookUrl;
        this.responsibleId = responsibleId;
    }

    @Override
    public Object getConnection(Subject subject, ConnectionRequestInfo cxRequestInfo) throws ResourceException {
        return new Bitrix24Connection(this, restClient, webhookUrl, responsibleId);
    }

    @Override
    public void destroy() throws ResourceException {
        listeners.clear();
    }

    @Override
    public void cleanup() throws ResourceException {
        // rest client не держит состояния
    }

    @Override
    public void associateConnection(Object connection) throws ResourceException {
        if (!(connection instanceof Bitrix24Connection)) {
            throw new ResourceException("Unsupported connection type: " + connection);
        }
    }

    @Override
    public void addConnectionEventListener(ConnectionEventListener listener) {
        listeners.add(listener);

    }

    @Override
    public void removeConnectionEventListener(ConnectionEventListener listener) {
        listeners.remove(listener);
    }

    @Override
    public XAResource getXAResource() throws ResourceException {
        throw new ResourceException("Bitrix24 connector does not support XA transactions");
    }

    @Override
    public LocalTransaction getLocalTransaction() throws ResourceException {
        throw new ResourceException("Bitrix24 connector does not support local transactions");
    }

    @Override
    public ManagedConnectionMetaData getMetaData() throws ResourceException {
        return new Bitrix24ManagedConnectionMetaData();
    }

    @Override
    public void setLogWriter(PrintWriter out) throws ResourceException {
        this.logWriter = out;
    }

    @Override
    public PrintWriter getLogWriter() throws ResourceException {
        return logWriter;
    }
}
