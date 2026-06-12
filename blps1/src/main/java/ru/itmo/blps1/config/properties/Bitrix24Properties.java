package ru.itmo.blps1.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "bitrix24")
public record Bitrix24Properties(
        String webhookUrl,
        Long responsibleId
) {
}