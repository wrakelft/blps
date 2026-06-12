package ru.itmo.blps1.messaging.producer;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.itmo.blps1.messaging.event.BoardModerationRequestEvent;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
@Slf4j
public class BoardModerationProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    private final ObjectMapper objectMapper;

    @Value("${app.kafka.topics.board-moderation-requests}")
    private String boardModerationRequestTopic;

    public void sendBoardModerationRequested(BoardModerationRequestEvent event) {
        String key = String.valueOf(event.boardId());
        String payload = toJson(event);

        kafkaTemplate.send(boardModerationRequestTopic, key, payload);

        log.info(
                "Sent board moderation event to Kafka: topic={}, key={}, boardId={}, requestedBy={}",
                boardModerationRequestTopic,
                key,
                event.boardId(),
                event.requestedByUsername()
        );
    }

    private String toJson(BoardModerationRequestEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to serialize board moderation event", exception);
        }
    }
}
