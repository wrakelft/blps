package ru.itmo.blps1.integration.bitrix24.jca;

import jakarta.resource.spi.ConnectionRequestInfo;

import java.util.Objects;

public class Bitrix24ConnectionRequestInfo implements ConnectionRequestInfo {

    private final String webhookUrl;
    private final Long responsibleId;

    public Bitrix24ConnectionRequestInfo(String webhookUrl, Long responsibleId) {
        this.webhookUrl = webhookUrl;
        this.responsibleId = responsibleId;
    }

    public String webhookUrl() {
        return webhookUrl;
    }

    public Long responsibleId() {
        return responsibleId;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if(!(object instanceof Bitrix24ConnectionRequestInfo that)) {
            return false;
        }

        return Objects.equals(webhookUrl, that.webhookUrl)
                && Objects.equals(responsibleId, that.responsibleId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(webhookUrl, responsibleId);
    }
}