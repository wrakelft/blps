package ru.itmo.blps1.messaging.consumer;

import ru.itmo.blps1.service.bpm.BusinessProcessService;
import tools.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import ru.itmo.blps1.messaging.event.BoardModerationRequestEvent;

@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(
        name = "app.kafka.consumer-enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class BoardModerationConsumer {

    private final ObjectMapper objectMapper;

    private final BusinessProcessService businessProcessService;

    @KafkaListener(
            topics = "${app.kafka.topics.board-moderation-requests}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consume(String payload) {
        log.info("Received board moderation event from Kafka: payload={}", payload);

        BoardModerationRequestEvent event = fromJson(payload);

        businessProcessService.correlateBoardModerationRequested(event);

        log.info(
                "Processed board moderation event: boardId={}, requestedBy={}",
                event.boardId(),
                event.requestedByUsername()
        );
    }

    private BoardModerationRequestEvent fromJson(String payload) {
        try {
            return objectMapper.readValue(payload, BoardModerationRequestEvent.class);
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to deserialize board moderation event", exception);
        }
    }
}