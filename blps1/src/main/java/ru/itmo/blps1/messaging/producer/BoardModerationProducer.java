package ru.itmo.blps1.messaging.producer;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class BoardModerationProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${app.kafka.topics.board-moderation-requests}")
    private String boardModerationRequestTopic;

    public void sendBoardModerationRequested(Long boardId, String payload) {
        String key = String.valueOf(boardId);

        kafkaTemplate.send(boardModerationRequestTopic, key, payload);

        log.info(
                "Sent board moderation event to Kafka: topic={}, key={}, payload={}",
                boardModerationRequestTopic,
                key,
                payload
        );
    }
}
