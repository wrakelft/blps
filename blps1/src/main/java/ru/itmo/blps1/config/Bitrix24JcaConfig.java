package ru.itmo.blps1.config;

import jakarta.resource.ResourceException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import ru.itmo.blps1.config.properties.Bitrix24Properties;
import ru.itmo.blps1.integration.bitrix24.jca.Bitrix24ConnectionFactory;
import ru.itmo.blps1.integration.bitrix24.jca.Bitrix24ManagedConnectionFactory;

@Configuration
public class Bitrix24JcaConfig {

    @Bean
    public Bitrix24ManagedConnectionFactory bitrix24ManagedConnectionFactory(
            RestClient.Builder restClientBuilder,
            Bitrix24Properties properties
    ) {
        return new Bitrix24ManagedConnectionFactory(
                restClientBuilder,
                properties.webhookUrl(),
                properties.responsibleId()
        );
    }

    @Bean
    public Bitrix24ConnectionFactory bitrix24ConnectionFactory(
            Bitrix24ManagedConnectionFactory managedConnectionFactory
    ) {
        try {
            return (Bitrix24ConnectionFactory) managedConnectionFactory.createConnectionFactory();
        } catch (ResourceException exception) {
            throw new IllegalStateException("Failed to create Bitrix24 JCA connection factory", exception);
        }
    }

    @Bean
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }
}