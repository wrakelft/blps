package ru.itmo.blps1.integration.bitrix24.jca;

import jakarta.resource.ResourceException;
import jakarta.resource.spi.ManagedConnectionMetaData;

public class Bitrix24ManagedConnectionMetaData implements ManagedConnectionMetaData {
    @Override
    public String getEISProductName() throws ResourceException {
        return "Bitrix24";
    }

    @Override
    public String getEISProductVersion() throws ResourceException {
        return "REST APO";
    }

    @Override
    public int getMaxConnections() throws ResourceException {
        return 0;
    }

    @Override
    public String getUserName() throws ResourceException {
        return "webhook-user";
    }
}
