package ru.itmo.blps1.integration.bitrix24;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.itmo.blps1.entity.BoardModerationRequest;
import ru.itmo.blps1.integration.bitrix24.jca.Bitrix24Connection;
import ru.itmo.blps1.integration.bitrix24.jca.Bitrix24ConnectionFactory;
import ru.itmo.blps1.integration.corporate.CorporateModerationConnector;
import ru.itmo.blps1.integration.corporate.dto.ExternalModerationTask;

@Component
@RequiredArgsConstructor
public class Bitrix24CorporateModerationConnector implements CorporateModerationConnector {

    private final Bitrix24ConnectionFactory connectionFactory;

    @Override
    public ExternalModerationTask createModerationTask(BoardModerationRequest request) {
        try (Bitrix24Connection connection = connectionFactory.getConnection()) {
            return connection.createTask(request);
        }
    }
}
