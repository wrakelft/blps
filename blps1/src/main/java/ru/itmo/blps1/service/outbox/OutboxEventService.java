package ru.itmo.blps1.service.outbox;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.itmo.blps1.entity.OutboxEvent;
import ru.itmo.blps1.entity.enums.OutboxEventStatus;
import ru.itmo.blps1.messaging.event.BoardModerationRequestEvent;
import ru.itmo.blps1.messaging.event.OutboxEventTypes;
import ru.itmo.blps1.repository.OutboxEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class OutboxEventService implements OutboxEventServiceInt {

    private final OutboxEventRepository outboxEventRepository;

    private final ObjectMapper objectMapper;

    @Override
    public void saveBoardModerationRequestedEvent(BoardModerationRequestEvent event) {
        String payload = toJson(event);

        OutboxEvent outboxEvent = OutboxEvent.builder()
                .eventType(OutboxEventTypes.BOARD_MODERATION_REQUESTED)
                .aggregateType("BOARD")
                .aggregateId(event.boardId())
                .payload(payload)
                .status(OutboxEventStatus.NEW)
                .retryCount(0)
                .build();

        outboxEventRepository.save(outboxEvent);
    }

    private String toJson(BoardModerationRequestEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to serialize outbox event", exception);
        }
    }
}
