package ru.itmo.blps1.service.bpm;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.blps1.entity.OutboxEvent;
import ru.itmo.blps1.entity.enums.OutboxEventStatus;
import ru.itmo.blps1.messaging.event.OutboxEventTypes;
import ru.itmo.blps1.messaging.producer.BoardModerationProducer;
import ru.itmo.blps1.repository.OutboxEventRepository;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxPublisherActionService {

    private final OutboxEventRepository outboxEventRepository;
    private final BoardModerationProducer boardModerationProducer;

    @Transactional
    public void publishOutboxEvents() {
        List<OutboxEvent> events = outboxEventRepository
                .findTop200ByStatusInOrderByCreatedAtAsc(
                        List.of(OutboxEventStatus.NEW, OutboxEventStatus.FAILED)
                );

        if (events.isEmpty()) {
            return;
        }

        log.info("Found {} outbox events to publish", events.size());

        for (OutboxEvent event : events) {
            try {
                publishEvent(event);

                event.setStatus(OutboxEventStatus.SENT);
                event.setSentAt(OffsetDateTime.now());
                event.setErrorMessage(null);

                log.info("Outbox event published successfully: id={}", event.getId());
            } catch (Exception exception) {
                event.setStatus(OutboxEventStatus.FAILED);
                event.setRetryCount(event.getRetryCount() + 1);
                event.setErrorMessage(exception.getMessage());

                log.error("Failed to publish outbox event: id={}", event.getId(), exception);
            }
        }
    }

    private void publishEvent(OutboxEvent event) {
        if (OutboxEventTypes.BOARD_MODERATION_REQUESTED.equals(event.getEventType())) {
            boardModerationProducer.sendBoardModerationRequested(
                    event.getAggregateId(),
                    event.getPayload()
            );
            return;
        }

        throw new IllegalArgumentException("Unsupported outbox event type: " + event.getEventType());
    }
}